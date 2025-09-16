# ufldv2_tflite_infer_io.py
import os
import sys
import cv2
import numpy as np
import argparse
import time

# tflite-runtime 우선, 없으면 tensorflow 사용
try:
    from tflite_runtime.interpreter import Interpreter
except Exception:
    import tensorflow as tf
    Interpreter = tf.lite.Interpreter  # type: ignore

# 프로젝트 루트 기준 utils.config 사용
sys.path.append(os.path.join(os.path.dirname(__file__), "../"))
from utils.config import Config  # type: ignore

IMG_EXTS = {".jpg", ".jpeg", ".png", ".bmp", ".webp"}
VID_EXTS = {".mp4", ".avi", ".mov", ".mkv", ".webm", ".m4v"}

def is_image_file(path: str) -> bool:
    return os.path.splitext(path)[1].lower() in IMG_EXTS

def is_video_file(path: str) -> bool:
    return os.path.splitext(path)[1].lower() in VID_EXTS

def softmax_np(x, axis=None):
    x = x - np.max(x, axis=axis, keepdims=True)
    e = np.exp(x)
    return e / np.sum(e, axis=axis, keepdims=True)

class UFLDv2TFLite:
    def __init__(self, model_path: str, config_path: str, ori_size=None, debug=True, num_threads=4):
        self.interpreter = Interpreter(model_path=model_path, num_threads=num_threads)
        self.interpreter.allocate_tensors()
        self.input_details = self.interpreter.get_input_details()
        self.output_details = self.interpreter.get_output_details()

        cfg = Config.fromfile(config_path)
        self.input_width = int(cfg.train_width)      # 1600
        self.input_height = int(cfg.train_height)    # 320
        self.num_row = int(cfg.num_row)              # 72
        self.num_col = int(cfg.num_col)              # 81
        self.num_lanes = int(cfg.num_lanes)          # 4
        self.crop_ratio = float(cfg.crop_ratio)      # 0.6
        print("num_lanes : " + str(self.num_lanes))
        self.num_cell_row = int(getattr(cfg, "num_cell_row", 200))
        self.num_cell_col = int(getattr(cfg, "num_cell_col", 100))

        # 원본 높이 기준 앵커
        self.row_anchor = np.linspace(1.0 - self.crop_ratio, 1.0, self.num_row, dtype=np.float32)  # 0.4~1.0
        self.col_anchor = np.linspace(0.0, 1.0, self.num_col, dtype=np.float32)                    # 0.0~1.0

        if ori_size is not None:
            self.ori_img_w, self.ori_img_h = ori_size
        else:
            self.ori_img_w, self.ori_img_h = None, None

        self.debug = debug
        if self.debug:
            print("[TFLite] Input:", self.input_details[0]["shape"], self.input_details[0]["dtype"])
            for od in self.output_details:
                print("[TFLite] Output:", od["name"], od["shape"], od["dtype"])

    # === [ADD] Lane-type & color classification utils =========================
    @staticmethod
    def _lane_color_masks(img_bgr: np.ndarray):
        """
        흰색/노란색 마스크 동시 반환 (0/255)
        """
        hsv = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2HSV)
        h, s, v = cv2.split(hsv)

        # White (HSV + LAB 보강)
        white_hsv = (s < 90) & (v > 170)
        lab = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2LAB)
        L, A, B = cv2.split(lab)
        white_lab = (L > 185)
        white_mask = ((white_hsv | white_lab).astype(np.uint8)) * 255

        # Yellow (대역 넓게 + LAB 보강)
        y1 = (h >= 8) & (h <= 65) & (s >= 25) & (v >= 110)
        y2 = (L > 150) & (B > 135) & (A > 110) & (A < 150)
        yellow_mask = ((y1 | y2).astype(np.uint8)) * 255
        k = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (3, 3))
        yellow_mask = cv2.morphologyEx(yellow_mask, cv2.MORPH_CLOSE, k, iterations=1)
        return white_mask, yellow_mask

    @staticmethod
    def _sample_along_curve(points, step: float = 3.0):
        if len(points) < 2:
            return [tuple(map(float, p)) for p in points]
        pts = np.array(points, dtype=np.float32)
        seg_lens = np.linalg.norm(pts[1:] - pts[:-1], axis=1)
        total = float(seg_lens.sum())
        if total < 1e-6:
            return [tuple(map(float, p)) for p in points]
        n_samples = int(max(2, total // step))
        s_targets = np.linspace(0, total, n_samples)

        out = []
        acc = 0.0
        j = 0
        for s in s_targets:
            while j < len(seg_lens) - 1 and acc + seg_lens[j] < s:
                acc += seg_lens[j]
                j += 1
            if j >= len(seg_lens):
                out.append(tuple(pts[-1]))
                continue
            t = (s - acc) / max(1e-6, seg_lens[j])
            p = pts[j] * (1 - t) + pts[j + 1] * t
            out.append((float(p[0]), float(p[1])))
        return out

    @staticmethod
    def _normal_direction(points, idx: int) -> np.ndarray:
        n = len(points)
        if n == 0:
            return np.array([1.0, 0.0], dtype=np.float32)
        if n == 1:
            return np.array([0.0, 1.0], dtype=np.float32)

        idx = max(0, min(idx, n - 1))
        i0 = max(0, idx - 1)
        i1 = min(n - 1, idx + 1)

        p0 = np.array(points[i0], dtype=np.float32)
        p1 = np.array(points[i1], dtype=np.float32)

        t = p1 - p0
        norm = np.linalg.norm(t)
        if norm < 1e-6:
            if i1 + 1 < n:
                t = np.array(points[i1 + 1], dtype=np.float32) - p1
            elif i0 - 1 >= 0:
                t = p0 - np.array(points[i0 - 1], dtype=np.float32)
            else:
                t = np.array([1.0, 0.0], dtype=np.float32)
            norm = np.linalg.norm(t)
            if norm < 1e-6:
                t = np.array([1.0, 0.0], dtype=np.float32)
                norm = 1.0

        t = t / norm
        nvec = np.array([-t[1], t[0]], dtype=np.float32)  # 법선
        return nvec

    @staticmethod
    def _runlength_gaps(signal: np.ndarray, min_gap: int = 6):
        gaps = []
        cur = 0
        for v in signal:
            if v == 0:
                cur += 1
            else:
                if cur >= min_gap:
                    gaps.append(cur)
                cur = 0
        if cur >= min_gap:
            gaps.append(cur)
        return gaps

    def classify_lane(self,
                      img_bgr: np.ndarray,
                      lane_points,
                      strip_half_width: int,
                      sample_step: float = 3.0,
                      cov_solid_default: float = 0.65,
                      cov_dashed_default: float = 0.45,
                      min_gap_pix: int = 6):
        """
        레인별 색상(white/yellow) 및 유형(solid/dashed/ambiguous) 판정
        """
        H, W = img_bgr.shape[:2]
        if len(lane_points) < 2:
            return {"label": "ambiguous", "color": "unknown",
                    "coverage": 0.0, "coverage_white": 0.0, "coverage_yellow": 0.0,
                    "fill_ratio": 0.0, "long_gap_count": 0,
                    "gaps": [], "signal": []}

        # 1) 스트립 마스크(그리기용)
        lane_mask = np.zeros((H, W), dtype=np.uint8)
        pts = np.array(lane_points, dtype=np.int32).reshape(-1, 1, 2)
        cv2.polylines(lane_mask, [pts], False, 255, thickness=strip_half_width * 2, lineType=cv2.LINE_AA)

        # 2) 커버리지 계산용 얇은 마스크(희석 방지)
        eval_th = max(3, strip_half_width)  # lane_mask보다 얇게
        eval_mask = np.zeros((H, W), dtype=np.uint8)
        cv2.polylines(eval_mask, [pts], False, 255, thickness=eval_th, lineType=cv2.LINE_AA)
        lane_pix = int(np.count_nonzero(eval_mask))
        if lane_pix == 0:
            return {"label": "ambiguous", "color": "unknown",
                    "coverage": 0.0, "coverage_white": 0.0, "coverage_yellow": 0.0,
                    "fill_ratio": 0.0, "long_gap_count": 0,
                    "gaps": [], "signal": []}

        # 3) 색상별 마스크
        white_mask, yellow_mask = self._lane_color_masks(img_bgr)

        white_in_lane = int(np.count_nonzero(cv2.bitwise_and(white_mask, white_mask, mask=eval_mask)))
        yellow_in_lane = int(np.count_nonzero(cv2.bitwise_and(yellow_mask, yellow_mask, mask=eval_mask)))
        cov_white = white_in_lane / lane_pix
        cov_yellow = yellow_in_lane / lane_pix

        # 4) 지배 색상 선택
        if cov_yellow >= max(0.15, cov_white * 1.10):
            base_color, base_mask = "yellow", yellow_mask
        elif cov_white >= 0.12:
            base_color, base_mask = "white", white_mask
        else:
            if cov_yellow > cov_white:
                base_color, base_mask = "yellow", yellow_mask
            else:
                base_color, base_mask = "white", white_mask

        # 5) 곡선을 따라 1D 존재 신호 생성(지배 색 기준)
        samples = self._sample_along_curve(lane_points, step=sample_step)
        signal = []
        for i, (x, y) in enumerate(samples):
            nvec = self._normal_direction(samples, i)
            has_color = 0
            for d in range(-strip_half_width, strip_half_width + 1):
                sx = int(round(x + nvec[0] * d))
                sy = int(round(y + nvec[1] * d))
                if 0 <= sx < W and 0 <= sy < H and base_mask[sy, sx] > 0:
                    has_color = 1
                    break
            signal.append(has_color)
        signal = np.array(signal, dtype=np.uint8)

        fill_ratio = float(signal.mean())
        gaps = self._runlength_gaps(signal, min_gap=min_gap_pix)
        long_gap_cnt = len(gaps)

        # 6) 실/점선 판정 (fill_ratio 우선 + 색상별 보정 임계)
        if base_color == "yellow":
            cov_solid, cov_dashed = 0.52, 0.35
        else:
            cov_solid, cov_dashed = cov_solid_default, cov_dashed_default

        if fill_ratio >= 0.85 and long_gap_cnt <= 1:
            label = "solid"
        elif fill_ratio <= 0.65 and long_gap_cnt >= 2:
            label = "dashed"
        else:
            cov_base = cov_yellow if base_color == "yellow" else cov_white
            if cov_base >= cov_solid and long_gap_cnt <= 1:
                label = "solid"
            elif cov_base <= cov_dashed and long_gap_cnt >= 2:
                label = "dashed"
            else:
                label = "ambiguous"

        cov_base = cov_yellow if base_color == "yellow" else cov_white
        return {
            "label": label,
            "color": base_color,
            "coverage": float(cov_base),
            "coverage_white": float(cov_white),
            "coverage_yellow": float(cov_yellow),
            "fill_ratio": fill_ratio,
            "long_gap_count": long_gap_cnt,
            "gaps": gaps,
            "signal": signal.tolist()
        }
    # === [ADD] =================================================================

    # 출력 텐서를 shape로 자동 매핑
    def _collect_outputs(self):
        outs = {}
        for od in self.output_details:
            name = od["name"]
            val = self.interpreter.get_tensor(od["index"])
            s = tuple(val.shape)
            if s == (1, self.num_cell_row, self.num_row, self.num_lanes):
                outs["loc_row"] = val
            elif s == (1, self.num_cell_col, self.num_col, self.num_lanes):
                outs["loc_col"] = val
            elif s == (1, 2, self.num_row, self.num_lanes):
                outs["exist_row"] = val
            elif s == (1, 2, self.num_col, self.num_lanes):
                outs["exist_col"] = val
            else:
                if self.debug:
                    print(f"[WARN] 매핑 불가 출력: {name} shape={s}")

        need = {"loc_row", "loc_col", "exist_row", "exist_col"}
        missing = need - set(outs.keys())
        if missing:
            raise RuntimeError(f"필수 출력 누락: {missing}. 모델 출력 shape를 확인하세요.")
        return outs

    def pred2coords(
        self,
        pred: dict,
        ori_w: int,
        ori_h: int,
        *,
        tau_row: float = 0.70,
        tau_col: float = 0.70,
        min_pts_row: int = 6,
        min_pts_col: int = 6,
        local_width: int = 1,
        row_lane_idx: tuple = (1, 2),
        col_lane_idx: tuple = (0, 3),
        sort_left_to_right: bool = True,
    ):
        """
        UFLD 계열 출력으로부터 (x,y) 좌표 시퀀스 리스트 생성
        """
        loc_row = pred["loc_row"]      # [1, 200, 72, 4]
        loc_col = pred["loc_col"]      # [1, 100, 81, 4]
        exist_row = pred["exist_row"]  # [1, 2, 72, 4]
        exist_col = pred["exist_col"]  # [1, 2, 81, 4]

        exist_row_prob = softmax_np(exist_row.astype(np.float32), axis=1)[0, 1]
        exist_col_prob = softmax_np(exist_col.astype(np.float32), axis=1)[0, 1]

        _, grid_row, num_row, num_lanes = loc_row.shape
        _, grid_col, num_col, _        = loc_col.shape

        valid_row   = np.argmax(exist_row, axis=1)  # [1, num_row, 4]
        valid_col   = np.argmax(exist_col, axis=1)  # [1, num_col, 4]
        max_idx_row = np.argmax(loc_row,  axis=1)   # [1, num_row, 4]
        max_idx_col = np.argmax(loc_col,  axis=1)   # [1, num_col, 4]

        cut_offset_px = int(ori_h * (1.0 - self.crop_ratio))
        bottom_h_px   = ori_h - cut_offset_px

        lanes = {i: [] for i in range(num_lanes)}

        def longest_run(indices: np.ndarray, weights: np.ndarray | None = None) -> np.ndarray:
            if indices.size == 0:
                return indices
            splits = np.where(np.diff(indices) > 1)[0] + 1
            groups = np.split(indices, splits)
            if weights is None:
                groups.sort(key=lambda g: len(g), reverse=True)
            else:
                groups.sort(key=lambda g: (len(g), float(weights[g].sum())), reverse=True)
            return groups[0]

        # ROW 기반 (y 고정, x 예측)
        for i in row_lane_idx:
            mask = (exist_row_prob[:, i] > tau_row) & (valid_row[0, :, i] == 1)
            active = np.where(mask)[0]
            if active.size < min_pts_row:
                continue
            active = longest_run(active, weights=exist_row_prob[:, i])
            if active.size < min_pts_row:
                continue

            tmp = []
            for k in active:
                center = int(max_idx_row[0, k, i])
                L = max(0, center - local_width)
                R = min(grid_row - 1, center + local_width)
                inds = np.arange(L, R + 1, dtype=np.float32)
                slice_logits = loc_row[0, L:R + 1, k, i].astype(np.float32)
                probs = softmax_np(slice_logits, axis=0)
                out = float(np.sum(probs * inds) + 0.5)

                x_px = (out / (grid_row - 1)) * ori_w
                y_px = int(self.row_anchor[k] * ori_h)
                tmp.append((int(x_px), int(y_px)))

            if tmp:
                tmp.sort(key=lambda p: p[1])
                lanes[i] = tmp

        # COL 기반 (x 고정, y 예측)
        for i in col_lane_idx:
            mask = (exist_col_prob[:, i] > tau_col) & (valid_col[0, :, i] == 1)
            active = np.where(mask)[0]
            if active.size < min_pts_col:
                continue
            active = longest_run(active, weights=exist_col_prob[:, i])
            if active.size < min_pts_col:
                continue

            tmp = []
            for k in active:
                center = int(max_idx_col[0, k, i])
                L = max(0, center - local_width)
                R = min(grid_col - 1, center + local_width)
                inds = np.arange(L, R + 1, dtype=np.float32)
                slice_logits = loc_col[0, L:R + 1, k, i].astype(np.float32)
                probs = softmax_np(slice_logits, axis=0)
                out = float(np.sum(probs * inds) + 0.5)

                y_px  = float((out / (grid_col - 1)) * ori_h)
                x_px  = float(self.col_anchor[k] * ori_w)
                tmp.append((int(x_px), int(y_px)))

            if tmp:
                tmp.sort(key=lambda p: p[1])
                lanes[i] = tmp

        coords = [pts for pts in lanes.values() if pts]
        if not coords:
            return []

        if sort_left_to_right:
            def lane_key(pts):
                ys = [p[1] for p in pts]
                y_cut = np.percentile(ys, 80)
                xs_bottom = [p[0] for p in pts if p[1] >= y_cut] or [p[0] for p in pts]
                xs_bottom.sort()
                return xs_bottom[len(xs_bottom) // 2]
            coords = sorted(coords, key=lane_key)
        else:
            coords = [lanes[i] for i in range(num_lanes) if lanes[i]]

        return coords

    def forward(self, img_bgr):
        # 원본 크기 자동 탐지
        if self.ori_img_w is None or self.ori_img_h is None:
            self.ori_img_h, self.ori_img_w = img_bgr.shape[:2]
        im0 = img_bgr.copy()

        # (1600x320) 입력 만들기
        h_full = int(self.input_height / self.crop_ratio)  # 320/0.6 ≈ 533
        img_resized_full = cv2.resize(img_bgr, (self.input_width, h_full), cv2.INTER_CUBIC)
        cut_offset = h_full - self.input_height            # ≈ 213
        img_resized = img_resized_full[cut_offset:, :, :]  # (320, 1600)

        # BGR->RGB, /255, 정규화
        img_rgb = cv2.cvtColor(img_resized, cv2.COLOR_BGR2RGB).astype(np.float32) / 255.0
        mean = np.array([0.485, 0.456, 0.406], dtype=np.float32).reshape(1, 1, 3)
        std = np.array([0.229, 0.224, 0.225], dtype=np.float32).reshape(1, 1, 3)
        img_norm = (img_rgb - mean) / std
        inp = img_norm[np.newaxis, ...].astype(np.float32)  # [1, 320, 1600, 3]
        start_time = time.time()
        # 인터프리터 입력(동적 리사이즈 대응)
        in0 = self.input_details[0]
        if tuple(in0["shape"]) != tuple(inp.shape):
            try:
                self.interpreter.resize_tensor_input(in0["index"], inp.shape, strict=True)
                self.interpreter.allocate_tensors()
                self.input_details = self.interpreter.get_input_details()
                self.output_details = self.interpreter.get_output_details()
            except Exception as e:
                if self.debug:
                    print("[WARN] 입력 리사이즈 실패, 원래 shape로 강제 입력:", in0["shape"], "err:", e)

        
        self.interpreter.set_tensor(self.input_details[0]["index"], inp)
        self.interpreter.invoke()
        print("inference_time : ", time.time() - start_time)

        # 출력 수집 + 좌표 복원
        preds = self._collect_outputs()
        if self.debug:
            for k, v in preds.items():
                try:
                    print(f"[OUT] {k:10s} shape={v.shape} min={float(v.min()):.3f} "
                          f"max={float(v.max()):.3f} mean={float(v.mean()):.3f}")
                except Exception:
                    print(f"[OUT] {k:10s} shape={v.shape}")

        coords = self.pred2coords(preds, ori_w=self.ori_img_w, ori_h=self.ori_img_h)

        # (옵션) 개발 중 중단점: LANE_BREAKPOINT=1 환경변수로만 활성화
        if self.debug and os.getenv("LANE_BREAKPOINT", "0") == "1":
            breakpoint()

        # 분류 + 그리기 (GUI 없음, 반환 이미지만 작성)
        drawn = im0.copy()
        lane_meta = []

        # 해상도 기반 스트립 폭
        strip_half = max(4, int(self.ori_img_w * 0.004))

        for lane_idx, lane in enumerate(coords[:self.num_lanes]):
            res = self.classify_lane(
                drawn, lane,
                strip_half_width=strip_half,
                sample_step=3.0,
                cov_solid_default=0.65,
                cov_dashed_default=0.45,
                min_gap_pix=6
            )
            lane_meta.append(res)

            # 선 색상: 지배색
            if res["color"] == "yellow":
                line_color = (0, 215, 255)   # BGR
            else:
                line_color = (235, 235, 235)

            thickness = 3 if res["label"] == "solid" else 2

            if len(lane) >= 2:
                cv2.polylines(drawn, [np.array(lane, dtype=np.int32)], False, line_color, thickness, cv2.LINE_AA)
            for (x, y) in lane:
                if 0 <= x < drawn.shape[1] and 0 <= y < drawn.shape[0]:
                    cv2.circle(drawn, (x, y), 2, (0, 255, 0), -1)

            bx, by = lane[-1]
            by = min(by + 14, drawn.shape[0] - 8)
            cv2.putText(
                drawn,
                f"{lane_idx}:{res['label']}-{res['color']}(cov={res['coverage']:.2f},fill={res['fill_ratio']:.2f})",
                (bx, by),
                cv2.FONT_HERSHEY_SIMPLEX, 0.55, line_color, 2, cv2.LINE_AA
            )

        self.last_lane_meta = lane_meta

        # 간단 진단
        try:
            exist_row = preds["exist_row"].astype(np.float32)
            exist_col = preds["exist_col"].astype(np.float32)
            er = softmax_np(exist_row, axis=1)[0, 1].mean()
            ec = softmax_np(exist_col, axis=1)[0, 1].mean()
            summary = " | ".join([f"{i}:{m['label']}-{m['color']}" for i, m in enumerate(lane_meta)])
            print(f"[diag] lanes={sum(len(l) for l in coords)} "
                  f"| exist_row≈{er:.3f}, exist_col≈{ec:.3f} | {summary}")
        except Exception:
            pass

        return drawn, coords


def get_args():
    p = argparse.ArgumentParser()
    p.add_argument("--config_path", default="configs/culane_res34.py", type=str)
    p.add_argument("--model_path", default="weights/culane_res34.tflite", type=str)
    p.add_argument("--input_path", default="example.mp4", type=str, help="입력 파일 경로(이미지 또는 비디오)")
    p.add_argument("--output_path", default=None, type=str, help="저장 파일 경로(지정하지 않으면 자동 생성)")
    p.add_argument("--ori_size", default=None,
                   type=lambda s: None if s in [None, "None", ""] else eval(s),
                   help="(W,H) 강제 원본 크기. 보통은 None(자동) 권장")
    p.add_argument("--debug", action="store_true")
    p.add_argument("--threads", type=int, default=4, help="TFLite Interpreter num_threads")
    return p.parse_args()


def main():
    args = get_args()

    isnet = UFLDv2TFLite(
        model_path=args.model_path,
        config_path=args.config_path,
        ori_size=args.ori_size,
        debug=args.debug,
        num_threads=args.threads,
    )

    path = args.input_path
    ext = os.path.splitext(path)[1].lower()

    if is_image_file(path):
        # 단일 이미지
        img = cv2.imread(path)
        if img is None:
            raise RuntimeError(f"이미지를 열 수 없습니다: {path}")

        drawn, _ = isnet.forward(img)

        # 저장 경로 결정
        if args.output_path:
            out_img = args.output_path
        else:
            stem = os.path.splitext(path)[0]
            out_img = f"{stem}_lane.jpg"

        ok = cv2.imwrite(out_img, drawn)
        print(f"[SAVE] image -> {out_img} ({'OK' if ok else 'FAIL'})")

    else:
        # 비디오
        cap = cv2.VideoCapture(path)
        if not cap.isOpened():
            raise RuntimeError(f"비디오를 열 수 없습니다: {path}")

        # 저장 경로/포맷
        if args.output_path:
            out_vid = args.output_path
        else:
            stem = os.path.splitext(path)[0]
            out_vid = f"{stem}_lane.mp4"

        fps = cap.get(cv2.CAP_PROP_FPS)
        if fps <= 1e-3:
            fps = 30.0
        width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
        height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))

        fourcc = cv2.VideoWriter_fourcc(*'mp4v')
        writer = cv2.VideoWriter(out_vid, fourcc, fps, (width, height))
        if not writer.isOpened():
            # 코덱 가용성 문제면 프레임 시퀀스로 대체 저장
            print(f"[WARN] VideoWriter 열기 실패 → 프레임 PNG 시퀀스로 저장합니다.")
            seq_dir = f"{os.path.splitext(out_vid)[0]}_frames"
            os.makedirs(seq_dir, exist_ok=True)
            frame_idx = 0
            while True:
                ok, frame = cap.read()
                if not ok:
                    break
                drawn, _ = isnet.forward(frame)
                cv2.imwrite(os.path.join(seq_dir, f"frame_{frame_idx:06d}.png"), drawn)
                frame_idx += 1
            cap.release()
            print(f"[SAVE] frames -> {seq_dir} ({frame_idx} frames)")
            return

        frame_idx = 0
        while True:
            ok, frame = cap.read()
            if not ok:
                break
            drawn, _ = isnet.forward(frame)
            writer.write(drawn)
            frame_idx += 1

        writer.release()
        cap.release()
        print(f"[SAVE] video -> {out_vid} ({frame_idx} frames)")

if __name__ == "__main__":
    main()

# ufldv2_tflite_infer_io.py
# import os
# import sys
# import cv2
# import numpy as np
# import argparse
# import time

# # tflite-runtime 우선, 없으면 tensorflow 사용
# try:
#     from tflite_runtime.interpreter import Interpreter
# except Exception:
#     import tensorflow as tf
#     Interpreter = tf.lite.Interpreter  # type: ignore

# # 프로젝트 루트 기준 utils.config 사용
# sys.path.append(os.path.join(os.path.dirname(__file__), "../"))
# from utils.config import Config  # type: ignore

# IMG_EXTS = {".jpg", ".jpeg", ".png", ".bmp", ".webp"}
# VID_EXTS = {".mp4", ".avi", ".mov", ".mkv", ".webm", ".m4v"}


# def is_image_file(path: str) -> bool:
#     return os.path.splitext(path)[1].lower() in IMG_EXTS


# def is_video_file(path: str) -> bool:
#     return os.path.splitext(path)[1].lower() in VID_EXTS


# def softmax_np(x, axis=None):
#     x = x - np.max(x, axis=axis, keepdims=True)
#     e = np.exp(x)
#     return e / np.sum(e, axis=axis, keepdims=True)


# class UFLDv2TFLite:
#     def __init__(self, model_path: str, config_path: str, ori_size=None, debug=True, num_threads=4):
#         self.interpreter = Interpreter(model_path=model_path, num_threads=num_threads)
#         self.interpreter.allocate_tensors()
#         self.input_details = self.interpreter.get_input_details()
#         self.output_details = self.interpreter.get_output_details()

#         cfg = Config.fromfile(config_path)
#         self.input_width = int(cfg.train_width)      # 1600
#         self.input_height = int(cfg.train_height)    # 320
#         self.num_row = int(cfg.num_row)              # 72
#         self.num_col = int(cfg.num_col)              # 81
#         self.num_lanes = int(cfg.num_lanes)          # 4
#         self.crop_ratio = float(cfg.crop_ratio)      # 0.6
#         print("num_lanes : " + str(self.num_lanes))
#         self.num_cell_row = int(getattr(cfg, "num_cell_row", 200))
#         self.num_cell_col = int(getattr(cfg, "num_cell_col", 100))

#         # 원본 높이 기준 앵커
#         self.row_anchor = np.linspace(1.0 - self.crop_ratio, 1.0, self.num_row, dtype=np.float32)  # 0.4~1.0
#         self.col_anchor = np.linspace(0.0, 1.0, self.num_col, dtype=np.float32)                    # 0.0~1.0

#         if ori_size is not None:
#             self.ori_img_w, self.ori_img_h = ori_size
#         else:
#             self.ori_img_w, self.ori_img_h = None, None

#         self.debug = debug
#         if self.debug:
#             print("[TFLite] Input:", self.input_details[0]["shape"], self.input_details[0]["dtype"])
#             for od in self.output_details:
#                 print("[TFLite] Output:", od["name"], od["shape"], od["dtype"])

#         # 안정화용 상태
#         self.last_lane_meta = []
#         self._last_labels = ["ambiguous"] * self.num_lanes  # 히스테리시스용

#     # === [ADD] Lane-type & color classification utils =========================
#     @staticmethod
#     def _lane_color_masks(img_bgr: np.ndarray):
#         """
#         흰색/노란색 마스크 동시 반환 (0/255)
#         """
#         hsv = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2HSV)
#         h, s, v = cv2.split(hsv)

#         # White (HSV + LAB 보강)
#         white_hsv = (s < 90) & (v > 170)
#         lab = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2LAB)
#         L, A, B = cv2.split(lab)
#         white_lab = (L > 185)
#         white_mask = ((white_hsv | white_lab).astype(np.uint8)) * 255

#         # Yellow (대역 넓게 + LAB 보강)
#         y1 = (h >= 8) & (h <= 65) & (s >= 25) & (v >= 110)
#         y2 = (L > 150) & (B > 135) & (A > 110) & (A < 150)
#         yellow_mask = ((y1 | y2).astype(np.uint8)) * 255
#         k = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (3, 3))
#         yellow_mask = cv2.morphologyEx(yellow_mask, cv2.MORPH_CLOSE, k, iterations=1)
#         return white_mask, yellow_mask

#     @staticmethod
#     def _sample_along_curve(points, step: float = 3.0):
#         if len(points) < 2:
#             return [tuple(map(float, p)) for p in points]
#         pts = np.array(points, dtype=np.float32)
#         seg_lens = np.linalg.norm(pts[1:] - pts[:-1], axis=1)
#         total = float(seg_lens.sum())
#         if total < 1e-6:
#             return [tuple(map(float, p)) for p in points]
#         n_samples = int(max(2, total // step))
#         s_targets = np.linspace(0, total, n_samples)

#         out = []
#         acc = 0.0
#         j = 0
#         for s in s_targets:
#             while j < len(seg_lens) - 1 and acc + seg_lens[j] < s:
#                 acc += seg_lens[j]
#                 j += 1
#             if j >= len(seg_lens):
#                 out.append(tuple(pts[-1]))
#                 continue
#             t = (s - acc) / max(1e-6, seg_lens[j])
#             p = pts[j] * (1 - t) + pts[j + 1] * t
#             out.append((float(p[0]), float(p[1])))
#         return out

#     @staticmethod
#     def _normal_direction(points, idx: int) -> np.ndarray:
#         n = len(points)
#         if n == 0:
#             return np.array([1.0, 0.0], dtype=np.float32)
#         if n == 1:
#             return np.array([0.0, 1.0], dtype=np.float32)

#         idx = max(0, min(idx, n - 1))
#         i0 = max(0, idx - 1)
#         i1 = min(n - 1, idx + 1)

#         p0 = np.array(points[i0], dtype=np.float32)
#         p1 = np.array(points[i1], dtype=np.float32)

#         t = p1 - p0
#         norm = np.linalg.norm(t)
#         if norm < 1e-6:
#             if i1 + 1 < n:
#                 t = np.array(points[i1 + 1], dtype=np.float32) - p1
#             elif i0 - 1 >= 0:
#                 t = p0 - np.array(points[i0 - 1], dtype=np.float32)
#             else:
#                 t = np.array([1.0, 0.0], dtype=np.float32)
#             norm = np.linalg.norm(t)
#             if norm < 1e-6:
#                 t = np.array([1.0, 0.0], dtype=np.float32)
#                 norm = 1.0

#         t = t / norm
#         nvec = np.array([-t[1], t[0]], dtype=np.float32)  # 법선
#         return nvec

#     @staticmethod
#     def _runlength_gaps(signal: np.ndarray, min_gap: int = 6):
#         gaps = []
#         cur = 0
#         for v in signal:
#             if v == 0:
#                 cur += 1
#             else:
#                 if cur >= min_gap:
#                     gaps.append(cur)
#                 cur = 0
#         if cur >= min_gap:
#             gaps.append(cur)
#         return gaps

#     # === [ADD] 스냅 보정 유틸 ================================================
#     @staticmethod
#     def _count_nonzero_in_roi(mask: np.ndarray, x0: int, y0: int, x1: int, y1: int) -> int:
#         if x0 >= x1 or y0 >= y1:
#             return 0
#         roi = mask[y0:y1, x0:x1]
#         return int(np.count_nonzero(roi))

#     def _local_color_score(self, base_mask: np.ndarray, x: int, y: int, r: int) -> int:
#         H, W = base_mask.shape[:2]
#         x0, x1 = max(0, x - r), min(W, x + r + 1)
#         y0, y1 = max(0, y - r), min(H, y + r + 1)
#         return self._count_nonzero_in_roi(base_mask, x0, y0, x1, y1)

#     def _snap_points_to_color(self, img_bgr: np.ndarray, points, r: int = 5, prefer_yellow_when_close: bool = True):
#         """각 포인트를 반경 r 내에서 흰/노란색 마스크 점수 최대 지점으로 스냅."""
#         if not points:
#             return points, "unknown"

#         white_mask, yellow_mask = self._lane_color_masks(img_bgr)
#         snapped = []
#         white_win, yellow_win = 0, 0

#         for (x, y) in points:
#             best = (x, y)
#             best_w = self._local_color_score(white_mask, x, y, r)
#             best_y = self._local_color_score(yellow_mask, x, y, r)

#             # 근방 탐색(±r의 작은 격자)
#             for dx in (-r, 0, r):
#                 for dy in (-r, 0, r):
#                     tx, ty = x + dx, y + dy
#                     w_score = self._local_color_score(white_mask, tx, ty, r // 2)
#                     y_score = self._local_color_score(yellow_mask, tx, ty, r // 2)

#                     improved = False
#                     if y_score > best_y:
#                         best, best_y, best_w = (tx, ty), y_score, w_score
#                         improved = True
#                     if w_score > best_w and not improved:
#                         best, best_w, best_y = (tx, ty), w_score, y_score

#             snapped.append(best)
#             if best_y > best_w:
#                 yellow_win += 1
#             elif best_w > best_y:
#                 white_win += 1

#         base_color = "yellow" if (yellow_win > white_win and prefer_yellow_when_close) else ("white" if white_win > 0 else "unknown")
#         return snapped, base_color

#     # === [ADD] 적응형 스트립 폭 =============================================
#     @staticmethod
#     def _adaptive_strip_half(y: int, H: int, W: int) -> int:
#         # 하단으로 갈수록 폭↑. W 기반 기본값 + y/H에 따른 보정(원근 반영)
#         base = max(4, int(0.004 * W))
#         gain = int(0.012 * W * ((y / max(1, H)) ** 2))  # 제곱 증가
#         return base + gain

#     def classify_lane(self,
#                       img_bgr: np.ndarray,
#                       lane_points,
#                       strip_half_width: int,
#                       sample_step: float = 3.0,
#                       cov_solid_default: float = 0.65,
#                       cov_dashed_default: float = 0.45,
#                       min_gap_pix: int = 6):
#         """
#         레인별 색상(white/yellow) 및 유형(solid/dashed/ambiguous) 판정
#         """
#         H, W = img_bgr.shape[:2]
#         if len(lane_points) < 2:
#             return {"label": "ambiguous", "color": "unknown",
#                     "coverage": 0.0, "coverage_white": 0.0, "coverage_yellow": 0.0,
#                     "fill_ratio": 0.0, "long_gap_count": 0,
#                     "gaps": [], "signal": []}

#         # 1) 스트립 마스크(그리기용)
#         lane_mask = np.zeros((H, W), dtype=np.uint8)
#         pts = np.array(lane_points, dtype=np.int32).reshape(-1, 1, 2)
#         cv2.polylines(lane_mask, [pts], False, 255, thickness=strip_half_width * 2, lineType=cv2.LINE_AA)

#         # 2) 커버리지 계산용 얇은 마스크(희석 방지)
#         eval_th = max(3, strip_half_width)  # lane_mask보다 얇게
#         eval_mask = np.zeros((H, W), dtype=np.uint8)
#         cv2.polylines(eval_mask, [pts], False, 255, thickness=eval_th, lineType=cv2.LINE_AA)
#         lane_pix = int(np.count_nonzero(eval_mask))
#         if lane_pix == 0:
#             return {"label": "ambiguous", "color": "unknown",
#                     "coverage": 0.0, "coverage_white": 0.0, "coverage_yellow": 0.0,
#                     "fill_ratio": 0.0, "long_gap_count": 0,
#                     "gaps": [], "signal": []}

#         # 3) 색상별 마스크
#         white_mask, yellow_mask = self._lane_color_masks(img_bgr)

#         white_in_lane = int(np.count_nonzero(cv2.bitwise_and(white_mask, white_mask, mask=eval_mask)))
#         yellow_in_lane = int(np.count_nonzero(cv2.bitwise_and(yellow_mask, yellow_mask, mask=eval_mask)))
#         cov_white = white_in_lane / lane_pix
#         cov_yellow = yellow_in_lane / lane_pix

#         # 4) 지배 색상 선택
#         if cov_yellow >= max(0.15, cov_white * 1.10):
#             base_color, base_mask = "yellow", yellow_mask
#         elif cov_white >= 0.12:
#             base_color, base_mask = "white", white_mask
#         else:
#             if cov_yellow > cov_white:
#                 base_color, base_mask = "yellow", yellow_mask
#             else:
#                 base_color, base_mask = "white", white_mask

#         # 5) 곡선을 따라 1D 존재 신호 생성(지배 색 기준) + 중값필터 + 런렙
#         samples = self._sample_along_curve(lane_points, step=sample_step)

#         def _median1d(arr: np.ndarray, k: int = 5) -> np.ndarray:
#             k = max(3, k | 1)  # 홀수
#             pad = k // 2
#             arrp = np.pad(arr, (pad, pad), mode="edge")
#             out = np.empty_like(arr)
#             for i in range(len(arr)):
#                 out[i] = np.median(arrp[i:i+k])
#             return out

#         signal = []
#         for i, (x, y) in enumerate(samples):
#             nvec = self._normal_direction(samples, i)
#             has_color = 0
#             for d in range(-strip_half_width, strip_half_width + 1):
#                 sx = int(round(x + nvec[0] * d))
#                 sy = int(round(y + nvec[1] * d))
#                 if 0 <= sx < W and 0 <= sy < H and base_mask[sy, sx] > 0:
#                     has_color = 1
#                     break
#             signal.append(has_color)
#         signal = np.asarray(signal, dtype=np.uint8)
#         if signal.size >= 5:
#             signal = _median1d(signal, k=5)

#         def _runs(arr: np.ndarray, val: int):
#             runs, cur = [], 0
#             for v in arr:
#                 if v == val:
#                     cur += 1
#                 else:
#                     if cur > 0:
#                         runs.append(cur)
#                     cur = 0
#             if cur > 0:
#                 runs.append(cur)
#             return runs

#         one_runs = _runs(signal, 1)
#         zero_runs = _runs(signal, 0)

#         fill_ratio   = float(signal.mean())
#         gap_len_sum  = int(np.sum(zero_runs))
#         gap_coverage = (gap_len_sum / max(1, len(signal)))
#         long_gap_cnt = sum(1 for g in zero_runs if g >= 1)
#         max_gap_len  = max(zero_runs) if zero_runs else 0

#         # 원근 보정: y 평균이 클수록(하단) 공백 임계 증가
#         ys = [p[1] for p in lane_points]
#         y_mean_norm = (np.mean(ys) / max(1, H)) if ys else 0.7  # 0~1
#         min_gap_pix_dynamic = int(min_gap_pix * (0.8 + 0.8 * y_mean_norm))
#         min_gap_pix_dynamic = max(4, min_gap_pix_dynamic)

#         # 6) 실/점선 판정 (gap 비율/최대 공백 + 색상 커버리지)
#         if base_color == "yellow":
#             cov_solid, cov_dashed = 0.52, 0.35
#         else:
#             cov_solid, cov_dashed = cov_solid_default, cov_dashed_default

#         cov_base = cov_yellow if base_color == "yellow" else cov_white

#         is_solid_like = (
#             fill_ratio >= 0.88 and
#             gap_coverage <= 0.08 and
#             max_gap_len < int(min_gap_pix_dynamic * 1.3) and
#             cov_base >= cov_solid
#         )

#         is_dashed_like = (
#             (gap_coverage >= 0.18 and long_gap_cnt >= 2) or
#             (max_gap_len >= int(min_gap_pix_dynamic * 1.6) and gap_coverage >= 0.12)
#         )
#         is_dashed_like = is_dashed_like and (cov_base >= cov_dashed * 0.8)

#         if is_solid_like and not is_dashed_like:
#             label = "solid"
#         elif is_dashed_like and not is_solid_like:
#             label = "dashed"
#         else:
#             if cov_base >= cov_solid and gap_coverage <= 0.10:
#                 label = "solid"
#             elif cov_base <= cov_dashed and gap_coverage >= 0.16:
#                 label = "dashed"
#             else:
#                 label = "ambiguous"

#         return {
#             "label": label,
#             "color": base_color,
#             "coverage": float(cov_base),
#             "coverage_white": float(cov_white),
#             "coverage_yellow": float(cov_yellow),
#             "fill_ratio": float(fill_ratio),
#             "long_gap_count": int(long_gap_cnt),
#             "gaps": list(map(int, zero_runs)),
#             "signal": signal.astype(int).tolist()
#         }

#     # 출력 텐서를 shape로 자동 매핑
#     def _collect_outputs(self):
#         outs = {}
#         for od in self.output_details:
#             name = od["name"]
#             val = self.interpreter.get_tensor(od["index"])
#             s = tuple(val.shape)
#             if s == (1, self.num_cell_row, self.num_row, self.num_lanes):
#                 outs["loc_row"] = val
#             elif s == (1, self.num_cell_col, self.num_col, self.num_lanes):
#                 outs["loc_col"] = val
#             elif s == (1, 2, self.num_row, self.num_lanes):
#                 outs["exist_row"] = val
#             elif s == (1, 2, self.num_col, self.num_lanes):
#                 outs["exist_col"] = val
#             else:
#                 if self.debug:
#                     print(f"[WARN] 매핑 불가 출력: {name} shape={s}")

#         need = {"loc_row", "loc_col", "exist_row", "exist_col"}
#         missing = need - set(outs.keys())
#         if missing:
#             raise RuntimeError(f"필수 출력 누락: {missing}. 모델 출력 shape를 확인하세요.")
#         return outs

#     def pred2coords(
#         self,
#         pred: dict,
#         ori_w: int,
#         ori_h: int,
#         *,
#         tau_row: float = 0.55,
#         tau_col: float = 0.55,
#         min_pts_row: int = 6,
#         min_pts_col: int = 6,
#         local_width: int = 1,
#         row_lane_idx: tuple = (1, 2),
#         col_lane_idx: tuple = (0, 3),
#         sort_left_to_right: bool = True,
#     ):
#         """
#         UFLD 계열 출력으로부터 (x,y) 좌표 시퀀스 리스트 생성
#         """
#         loc_row = pred["loc_row"]      # [1, 200, 72, 4]
#         loc_col = pred["loc_col"]      # [1, 100, 81, 4]
#         exist_row = pred["exist_row"]  # [1, 2, 72, 4]
#         exist_col = pred["exist_col"]  # [1, 2, 81, 4]

#         exist_row_prob = softmax_np(exist_row.astype(np.float32), axis=1)[0, 1]
#         exist_col_prob = softmax_np(exist_col.astype(np.float32), axis=1)[0, 1]

#         _, grid_row, num_row, num_lanes = loc_row.shape
#         _, grid_col, num_col, _        = loc_col.shape

#         valid_row   = np.argmax(exist_row, axis=1)  # [1, num_row, 4]
#         valid_col   = np.argmax(exist_col, axis=1)  # [1, num_col, 4]
#         max_idx_row = np.argmax(loc_row,  axis=1)   # [1, num_row, 4]
#         max_idx_col = np.argmax(loc_col,  axis=1)   # [1, num_col, 4]

#         lanes = {i: [] for i in range(num_lanes)}

#         def longest_run(indices: np.ndarray, weights: np.ndarray | None = None) -> np.ndarray:
#             if indices.size == 0:
#                 return indices
#             splits = np.where(np.diff(indices) > 1)[0] + 1
#             groups = np.split(indices, splits)
#             if weights is None:
#                 groups.sort(key=lambda g: len(g), reverse=True)
#             else:
#                 groups.sort(key=lambda g: (len(g), float(weights[g].sum())), reverse=True)
#             return groups[0]

#         # ROW 기반 (y 고정, x 예측)
#         for i in row_lane_idx:
#             mask = (exist_row_prob[:, i] > tau_row) & (valid_row[0, :, i] == 1)
#             active = np.where(mask)[0]
#             if active.size < min_pts_row:
#                 continue
#             active = longest_run(active, weights=exist_row_prob[:, i])
#             if active.size < min_pts_row:
#                 continue

#             tmp = []
#             for k in active:
#                 center = int(max_idx_row[0, k, i])
#                 L = max(0, center - local_width)
#                 R = min(grid_row - 1, center + local_width)
#                 inds = np.arange(L, R + 1, dtype=np.float32)
#                 slice_logits = loc_row[0, L:R + 1, k, i].astype(np.float32)
#                 probs = softmax_np(slice_logits, axis=0)
#                 out = float(np.sum(probs * inds) + 0.5)

#                 x_px = (out / (grid_row - 1)) * ori_w
#                 y_px = int(self.row_anchor[k] * ori_h)
#                 tmp.append((int(x_px), int(y_px)))

#             if tmp:
#                 tmp.sort(key=lambda p: p[1])
#                 lanes[i] = tmp

#         # COL 기반 (x 고정, y 예측)
#         for i in col_lane_idx:
#             mask = (exist_col_prob[:, i] > tau_col) & (valid_col[0, :, i] == 1)
#             active = np.where(mask)[0]
#             if active.size < min_pts_col:
#                 continue
#             active = longest_run(active, weights=exist_col_prob[:, i])
#             if active.size < min_pts_col:
#                 continue

#             tmp = []
#             for k in active:
#                 center = int(max_idx_col[0, k, i])
#                 L = max(0, center - local_width)
#                 R = min(grid_col - 1, center + local_width)
#                 inds = np.arange(L, R + 1, dtype=np.float32)
#                 slice_logits = loc_col[0, L:R + 1, k, i].astype(np.float32)
#                 probs = softmax_np(slice_logits, axis=0)
#                 out = float(np.sum(probs * inds) + 0.5)

#                 y_px  = float((out / (grid_col - 1)) * ori_h)
#                 x_px  = float(self.col_anchor[k] * ori_w)
#                 tmp.append((int(x_px), int(y_px)))

#             if tmp:
#                 tmp.sort(key=lambda p: p[1])
#                 lanes[i] = tmp

#         coords = [pts for pts in lanes.values() if pts]
#         if not coords:
#             return []

#         if sort_left_to_right:
#             def lane_key(pts):
#                 ys = [p[1] for p in pts]
#                 y_cut = np.percentile(ys, 80)
#                 xs_bottom = [p[0] for p in pts if p[1] >= y_cut] or [p[0] for p in pts]
#                 xs_bottom.sort()
#                 return xs_bottom[len(xs_bottom) // 2]
#             coords = sorted(coords, key=lane_key)
#         else:
#             coords = [lanes[i] for i in range(num_lanes) if lanes[i]]

#         return coords

#     def forward(self, img_bgr):
#         # 원본 크기 자동 탐지
#         if self.ori_img_w is None or self.ori_img_h is None:
#             self.ori_img_h, self.ori_img_w = img_bgr.shape[:2]
#         im0 = img_bgr.copy()

#         # (1600x320) 입력 만들기
#         h_full = int(self.input_height / self.crop_ratio)  # 320/0.6 ≈ 533
#         img_resized_full = cv2.resize(img_bgr, (self.input_width, h_full), cv2.INTER_CUBIC)
#         cut_offset = h_full - self.input_height            # ≈ 213
#         img_resized = img_resized_full[cut_offset:, :, :]  # (320, 1600)

#         # BGR->RGB, /255, 정규화
#         img_rgb = cv2.cvtColor(img_resized, cv2.COLOR_BGR2RGB).astype(np.float32) / 255.0
#         mean = np.array([0.485, 0.456, 0.406], dtype=np.float32).reshape(1, 1, 3)
#         std = np.array([0.229, 0.224, 0.225], dtype=np.float32).reshape(1, 1, 3)
#         img_norm = (img_rgb - mean) / std
#         inp = img_norm[np.newaxis, ...].astype(np.float32)  # [1, 320, 1600, 3]

#         start_time = time.time()
#         # 인터프리터 입력(동적 리사이즈 대응)
#         in0 = self.input_details[0]
#         if tuple(in0["shape"]) != tuple(inp.shape):
#             try:
#                 self.interpreter.resize_tensor_input(in0["index"], inp.shape, strict=True)
#                 self.interpreter.allocate_tensors()
#                 self.input_details = self.interpreter.get_input_details()
#                 self.output_details = self.interpreter.get_output_details()
#             except Exception as e:
#                 if self.debug:
#                     print("[WARN] 입력 리사이즈 실패, 원래 shape로 강제 입력:", in0["shape"], "err:", e)

#         self.interpreter.set_tensor(self.input_details[0]["index"], inp)
#         self.interpreter.invoke()
#         print("inference_time : ", time.time() - start_time)

#         # 출력 수집 + 좌표 복원
#         preds = self._collect_outputs()
#         if self.debug:
#             for k, v in preds.items():
#                 try:
#                     print(f"[OUT] {k:10s} shape={v.shape} min={float(v.min()):.3f} "
#                           f"max={float(v.max()):.3f} mean={float(v.mean()):.3f}")
#                 except Exception:
#                     print(f"[OUT] {k:10s} shape={v.shape}")

#         coords = self.pred2coords(preds, ori_w=self.ori_img_w, ori_h=self.ori_img_h)

#         # (옵션) 개발 중 중단점: LANE_BREAKPOINT=1 환경변수로만 활성화
#         if self.debug and os.getenv("LANE_BREAKPOINT", "0") == "1":
#             breakpoint()

#         # === [ADD] 스냅 보정 적용 ============================================
#         drawn = im0.copy()
#         lane_meta = []

#         # 스냅 탐색 반경(해상도 비례)
#         snap_r = max(3, int(self.ori_img_w * 0.0035))

#         refined_coords = []
#         for lane in coords[:self.num_lanes]:
#             snapped, _ = self._snap_points_to_color(drawn, lane, r=snap_r)
#             refined_coords.append(snapped)

#         # 분류 + 그리기
#         for lane_idx, lane in enumerate(refined_coords):
#             if not lane:
#                 continue

#             # 적응형 스트립 폭(원근 반영)
#             ys = [p[1] for p in lane]
#             y_ref = int(np.percentile(ys, 80))  # 하단부 대표 y
#             strip_half = self._adaptive_strip_half(y_ref, self.ori_img_h, self.ori_img_w)

#             # 폭 증가 시 점선 공백 임계도 증가
#             min_gap_pix = max(6, strip_half)

#             res = self.classify_lane(
#                 drawn, lane,
#                 strip_half_width=strip_half,
#                 sample_step=3.0,
#                 cov_solid_default=0.65,
#                 cov_dashed_default=0.45,
#                 min_gap_pix=min_gap_pix
#             )

#             # 히스테리시스(경계 구간에서 이전 결정을 유지)
#             label_now = res["label"]
#             prev = self._last_labels[lane_idx] if lane_idx < len(self._last_labels) else "ambiguous"
#             if label_now == "ambiguous" and prev in ("solid", "dashed"):
#                 res["label"] = prev
#             else:
#                 if lane_idx < len(self._last_labels):
#                     self._last_labels[lane_idx] = label_now

#             lane_meta.append(res)

#             # 선 색상: 지배색
#             if res["color"] == "yellow":
#                 line_color = (0, 215, 255)   # BGR
#             else:
#                 line_color = (235, 235, 235)

#             thickness = 3 if res["label"] == "solid" else 2

#             if len(lane) >= 2:
#                 cv2.polylines(drawn, [np.array(lane, dtype=np.int32)], False, line_color, thickness, cv2.LINE_AA)
#             for (x, y) in lane:
#                 if 0 <= x < drawn.shape[1] and 0 <= y < drawn.shape[0]:
#                     cv2.circle(drawn, (x, y), 2, (0, 255, 0), -1)

#             bx, by = lane[-1]
#             by = min(by + 14, drawn.shape[0] - 8)
#             cv2.putText(
#                 drawn,
#                 f"{lane_idx}:{res['label']}-{res['color']}(cov={res['coverage']:.2f},fill={res['fill_ratio']:.2f})",
#                 (bx, by),
#                 cv2.FONT_HERSHEY_SIMPLEX, 0.55, line_color, 2, cv2.LINE_AA
#             )

#         self.last_lane_meta = lane_meta

#         # 간단 진단
#         try:
#             exist_row = preds["exist_row"].astype(np.float32)
#             exist_col = preds["exist_col"].astype(np.float32)
#             er = softmax_np(exist_row, axis=1)[0, 1].mean()
#             ec = softmax_np(exist_col, axis=1)[0, 1].mean()
#             summary = " | ".join([f"{i}:{m['label']}-{m['color']}" for i, m in enumerate(lane_meta)])
#             print(f"[diag] lanes={sum(len(l) for l in refined_coords)} "
#                   f"| exist_row≈{er:.3f}, exist_col≈{ec:.3f} | {summary}")
#         except Exception:
#             pass

#         return drawn, refined_coords


# def get_args():
#     p = argparse.ArgumentParser()
#     p.add_argument("--config_path", default="configs/culane_res34.py", type=str)
#     p.add_argument("--model_path", default="weights/culane_res34.tflite", type=str)
#     p.add_argument("--input_path", default="example.mp4", type=str, help="입력 파일 경로(이미지 또는 비디오)")
#     p.add_argument("--output_path", default=None, type=str, help="저장 파일 경로(지정하지 않으면 자동 생성)")
#     p.add_argument("--ori_size", default=None,
#                    type=lambda s: None if s in [None, "None", ""] else eval(s),
#                    help="(W,H) 강제 원본 크기. 보통은 None(자동) 권장")
#     p.add_argument("--debug", action="store_true")
#     p.add_argument("--threads", type=int, default=4, help="TFLite Interpreter num_threads")
#     return p.parse_args()


# def main():
#     args = get_args()

#     isnet = UFLDv2TFLite(
#         model_path=args.model_path,
#         config_path=args.config_path,
#         ori_size=args.ori_size,
#         debug=args.debug,
#         num_threads=args.threads,
#     )

#     path = args.input_path

#     if is_image_file(path):
#         # 단일 이미지
#         img = cv2.imread(path)
#         if img is None:
#             raise RuntimeError(f"이미지를 열 수 없습니다: {path}")

#         drawn, _ = isnet.forward(img)

#         # 저장 경로 결정
#         if args.output_path:
#             out_img = args.output_path
#         else:
#             stem = os.path.splitext(path)[0]
#             out_img = f"{stem}_lane.jpg"

#         ok = cv2.imwrite(out_img, drawn)
#         print(f"[SAVE] image -> {out_img} ({'OK' if ok else 'FAIL'})")

#     else:
#         # 비디오
#         cap = cv2.VideoCapture(path)
#         if not cap.isOpened():
#             raise RuntimeError(f"비디오를 열 수 없습니다: {path}")

#         # 저장 경로/포맷
#         if args.output_path:
#             out_vid = args.output_path
#         else:
#             stem = os.path.splitext(path)[0]
#             out_vid = f"{stem}_lane.mp4"

#         fps = cap.get(cv2.CAP_PROP_FPS)
#         if fps <= 1e-3:
#             fps = 30.0
#         width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
#         height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))

#         fourcc = cv2.VideoWriter_fourcc(*'mp4v')
#         writer = cv2.VideoWriter(out_vid, fourcc, fps, (width, height))
#         if not writer.isOpened():
#             # 코덱 가용성 문제면 프레임 시퀀스로 대체 저장
#             print(f"[WARN] VideoWriter 열기 실패 → 프레임 PNG 시퀀스로 저장합니다.")
#             seq_dir = f"{os.path.splitext(out_vid)[0]}_frames"
#             os.makedirs(seq_dir, exist_ok=True)
#             frame_idx = 0
#             while True:
#                 ok, frame = cap.read()
#                 if not ok:
#                     break
#                 drawn, _ = isnet.forward(frame)
#                 cv2.imwrite(os.path.join(seq_dir, f"frame_{frame_idx:06d}.png"), drawn)
#                 frame_idx += 1
#             cap.release()
#             print(f"[SAVE] frames -> {seq_dir} ({frame_idx} frames)")
#             return

#         frame_idx = 0
#         while True:
#             ok, frame = cap.read()
#             if not ok:
#                 break
#             drawn, _ = isnet.forward(frame)
#             writer.write(drawn)
#             frame_idx += 1

#         writer.release()
#         cap.release()
#         print(f"[SAVE] video -> {out_vid} ({frame_idx} frames)")


# if __name__ == "__main__":
#     main()

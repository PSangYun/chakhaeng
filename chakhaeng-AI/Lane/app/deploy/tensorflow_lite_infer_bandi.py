import os, sys
import cv2
import numpy as np

import argparse

import time

try:
    from tflite_runtime.interpreter import Interpreter
except Exception:
    import tensorflow as tf
    Interpreter = tf.lite.Interpreter

# 프로젝트 루트 기준 utils.config 사용
sys.path.append(os.path.join(os.path.dirname(__file__), "../"))
from utils.config import Config  # type: ignore

def get_args():
    p = argparse.ArgumentParser()
    p.add_argument("--config_path", default="configs/culane_res18.py", type=str)
    p.add_argument("--model_path", default="weights/culane_res18_dynamic.tflite", type=str)
    p.add_argument("--input_path", type=str, help="입력 파일 경로(이미지 또는 비디오)")
    p.add_argument("--output_path", default=None, type=str, help="저장 파일 경로(지정하지 않으면 자동 생성)")
    p.add_argument("--ori_size", default=None,
                   type=lambda s: None if s in [None, "None", ""] else eval(s),
                   help="(W,H) 강제 원본 크기. 보통은 None(자동) 권장")
    p.add_argument("--debug", action="store_true")
    p.add_argument("--threads", type=int, default=4, help="TFLite Interpreter num_threads")
    p.add_argument("--tau_row", type=float, default=0.55, help="존재 확률 임계값")
    p.add_argument("--tau_col", type=float, default=0.55, help="존재 확률 임계값")
    p.add_argument("--min_pts_row", type=int, default=10, help="한 레인의 최소 포인트 수. 이보다 적으면 그 레인을 버림.")
    p.add_argument("--min_pts_col", type=int, default=10, help="한 레인의 최소 포인트 수. 이보다 적으면 그 레인을 버림.")
    p.add_argument("--local_width", type=int, default=10, help="argmax 주변 로컬 윈도우 반경. ±local_width 범위를 소프트맥스로 가중평균해 서브셀 보정.")
    #pred2coords
    p.add_argument("--gap_tol", type=int, default=1, help="연속으로 간주할 최대 anchor 갭")
    #p.add_argument("--gap_tol_px", type=float, default=8.0, help="연속으로 간주할 최대 픽셀 갭")
    return p.parse_args()

IMG_EXTS = {".jpg", ".jpeg", ".png", ".bmp", ".webp"}
#IMAGE_EXTENSIONS, 이미지 확장자
VID_EXTS = {".mp4", ".avi", ".mov", ".mkv", ".webm", ".m4v"}

def is_image_file(path: str) -> bool:
    return os.path.splitext(path)[1].lower() in IMG_EXTS
    # splitext -> 가장 끝에 오는 "." 을 기준으로 파일명 분리
def is_video_file(path: str) -> bool:
    return os.path.splitext(path)[1].lower() in VID_EXTS

def softmax_np(x, axis=None):
    x = x - np.max(x, axis=axis, keepdims=True)
    e = np.exp(x)
    return e / np.sum(e, axis=axis, keepdims=True)

class UFLDv2TFLite:
    def __init__(self, model_path: str, config_path: str, args, ori_size=None, debug=True, num_threads=4):
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
        self.num_cell_row = int(getattr(cfg, "num_cell_row", 200))
        self.num_cell_col = int(getattr(cfg, "num_cell_col", 100))
        self.args = args
        if ori_size is not None:
            self.ori_img_w, self.ori_img_h = ori_size
        else:
            self.ori_img_w, self.ori_img_h = None, None

        self.debug = debug
        if self.debug:
            print("[TFLite] Input:", self.input_details[0]["shape"], self.input_details[0]["dtype"])
            for od in self.output_details:
                print("[TFLite] Output:", od["name"], od["shape"], od["dtype"])
        # 원본 높이 기준 앵커_차선 검출을 위한 기준점(row/col anchor) 좌표를 균일 간격으로 생성
        self.row_anchor = np.linspace(1.0 - self.crop_ratio, 1.0, self.num_row, dtype=np.float32)  # 0.4~1.0
        self.col_anchor = np.linspace(0.0, 1.0, self.num_col, dtype=np.float32)  

    def data_preprocess(self, img):
        # 원본 크기 자동 탐지
        if self.ori_img_w is None or self.ori_img_h is None:
            self.ori_img_h, self.ori_img_w = img.shape[:2]
        
        img_bgr = img.copy()
        # tensorflow_lite 입력에 맞게 이미지 사이즈 조정
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
        
        return inp
    
    def forward(self, img_bgr):
        
        inp = self.data_preprocess(img_bgr)

        start_time = time.time()
        self.interpreter.set_tensor(self.input_details[0]["index"], inp)
        self.interpreter.invoke()
        print("inference_time : ", time.time() - start_time)

        preds = self._collect_outputs()
        coords = self.pred2coords(preds, ori_w=self.ori_img_w, ori_h=self.ori_img_h, tau_row=self.args.tau_row, tau_col=self.args.tau_col, min_pts_col=self.args.min_pts_col, min_pts_row=self.args.min_pts_row, local_width=self.args.local_width)

        # 그리기
        drawn = img_bgr.copy()
        lane_meta = []

        # 차선 후보를 찾을 때 중심 좌표 기준으로 좌우 몇 픽셀 범위를 함께 탐색할지 결정하는, 해상도 비례 탐색 윈도우 반폭 값.
        strip_half = max(4, int(self.ori_img_w * 0.004)) # 즉, “탐색 보정용 폭”을 정하는 값.

        for lane_idx, lane in enumerate(coords[:self.num_lanes]):
            for (x, y) in lane:
                if 0 <= x < drawn.shape[1] and 0 <= y < drawn.shape[0]:
                    cv2.circle(drawn, (x, y), 2, (0, 255, 0), -1)

        return drawn, coords

    def pred2coords(
        self,
        pred: dict,
        ori_w: int,
        ori_h: int,
        *,
        tau_row: float = 0.80,
        tau_col: float = 0.80,
        min_pts_row: int = 10,
        min_pts_col: int = 10,
        local_width: int = 1,
        row_lane_idx: tuple = (1, 2),
        col_lane_idx: tuple = (0, 3),
        sort_left_to_right: bool = True,
        ):

        loc_row = pred["loc_row"]      # [1, 200, 72, 4]
        loc_col = pred["loc_col"]      # [1, 100, 81, 4]
        exist_row = pred["exist_row"]  # [1, 2, 72, 4]
        exist_col = pred["exist_col"]  # [1, 2, 81, 4]
        
        exist_row_prob = softmax_np(exist_row.astype(np.float32), axis=1)[0, 1]#[:, 1, :, :] 는 존재 여부에 대한 확률값을 가지고 있음
        exist_col_prob = softmax_np(exist_col.astype(np.float32), axis=1)[0, 1]
        
        _, grid_row, num_row, num_lanes = loc_row.shape
        _, grid_col, num_col, _ = loc_col.shape

        valid_row   = np.argmax(exist_row, axis=1)  # [1, num_row, 4]
        valid_col   = np.argmax(exist_col, axis=1)  # [1, num_col, 4]
        max_idx_row = np.argmax(loc_row,  axis=1)   # [1, num_row, 4]
        max_idx_col = np.argmax(loc_col,  axis=1)   # [1, num_col, 4]

        lanes = {i: [] for i in range(num_lanes)}

        #띄엄띄엄 잡힌 앵커들 중 가장 길고(신뢰 높은) 연속적인 구간만 사용 → 잘못된 점/구멍 필터링.
        def longest_run(
                indices: np.asarray, 
                weights: np.ndarray | None = None, 
                gap_tol: int = 1, # 인덱스 단위 허용 갭
                anchors: np.ndarray | None = None, # 정규화된 앵커 배열(row_anchor/col_anchor)
                ori_size: int | None = None, # 원본 이미지 크기 (픽셀 변환용)
                gap_tol_px: float | None = None # 픽셀 단위 허용 거리. None이면 픽셀 기준 무시
            ) -> np.ndarray:
            if indices.size == 0: # 빈 입력 방어
                return indices
            splits = np.where(np.diff(indices) > gap_tol)[0] + 1 #np.diff : 원소간 차이(i1 - i0...)를 계산해 반환 / np.where(조건문) 각 원소에 대해 조건을 만족할 경우 True 반환
            groups = np.split(indices, splits) #splits 값이 True 일 경우 이전까지의 원소를 그릅화
            if weights is None: #길이가 긴 구간을 더 신뢰, 내림차순 정렬
                groups.sort(key=lambda g: len(g), reverse=True) 
            else: #길이가 같을 경우에는 해당 구간의 가중치 합(존재확률 합)이 큰 걸 우선
                groups.sort(key=lambda g: (len(g), float(weights[g].sum())), reverse=True) #weight[g]는 fancy indexing으로 그 구간에 해당하는 가중치만 반환
            return groups[0] #정렬 후 가장 우선순위 높은 연속 구간을 반환
        
        # ROW 기반 (y 고정, x 예측)
        for i in row_lane_idx:
            mask = (exist_row_prob[:, i] > tau_row) & (valid_row[0, :, i] == 1)
            active = np.where(mask)[0]
            if active.size < min_pts_row:
                continue
            active = longest_run(active, weights=exist_row_prob[:, i], gap_tol=self.args.gap_tol)
            if active.size < min_pts_row:
                continue

            tmp = []
            for k in active:
                center = int(max_idx_row[0, k, i])
                L = max(0, center - local_width)
                R = min(grid_row - 1, center + local_width)
                inds = np.arange(L, R + 1, dtype=np.float32) #L 부터 R 까지 1씩 증가하는 값을 원소를 가지는 np.array 생성
                slice_logits = loc_row[0, L:R + 1, k, i].astype(np.float32)
                probs = softmax_np(slice_logits, axis=0)
                out = float(np.sum(probs * inds) + 0.5) #가중치를 반영한 anchor index 값

                x_px = (out / (grid_row - 1)) * ori_w
                y_px = self.row_anchor[k] * ori_h # 네트워크 출력(셀/anchor 단위)을 실제 원본 이미지 좌표계(px)로 매핑
                tmp.append((int(x_px), int(y_px)))

            if tmp:
                tmp.sort(key=lambda p: p[1])
                lanes[i] = tmp

        # COL 기반 (x 고정, y 예측)
        for i in col_lane_idx:
            mask = (exist_col_prob[:, i] > tau_col) & (valid_col[0, :, i] == 1) #존재 확률과 
            active = np.where(mask)[0]
            if active.size < min_pts_col:
                continue
            active = longest_run(active, weights=exist_col_prob[:, i], gap_tol=self.args.gap_tol)
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

def main():
    args = get_args()

    model = UFLDv2TFLite(
        model_path=args.model_path,
        config_path=args.config_path,
        args=args,
        ori_size=args.ori_size,
        debug=args.debug,
        num_threads=args.threads,
    )

    path = args.input_path
    ext = os.path.splitext(path)[1].lower()
    # 단일 이미지
    if is_image_file(path):
        img = cv2.imread(path)
        if img is None:
            raise RuntimeError(f"이미지를 열 수 없습니다: {path}")

        drawn, _ = model.forward(img)

        # 저장 경로 결정
        if args.output_path:
            out_img = args.output_path
        else:
            stem = os.path.splitext(path)[0]
            out_img = f"{stem}_lane.jpg"

        ok = cv2.imwrite(out_img, drawn)
        print(f"[SAVE] image -> {out_img} ({'OK' if ok else 'FAIL'})")
    # 비디오
    else:
        cap = cv2.VideoCapture(path)
        if not cap.isOpened():
            raise RuntimeError(f"비디오를 열 수 없습니다: {path}")
        
        if args.output_path: # 저장 경로/포맷
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
        if not writer.isOpened(): # 코덱 가용성 문제면 프레임 시퀀스로 대체 저장
            print(f"[WARN] VideoWriter 열기 실패 → 프레임 PNG 시퀀스로 저장합니다.")
            seq_dir = f"{os.path.splitext(out_vid)[0]}_frames"
            os.makedirs(seq_dir, exist_ok=True)
            frame_idx = 0
            while True:
                ok, frame = cap.read()
                if not ok:
                    break
                drawn, _ = model.forward(frame)
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
            drawn, _ = model.forward(frame)
            writer.write(drawn)
            frame_idx += 1

        writer.release()
        cap.release()
        print(f"[SAVE]")

if __name__ == "__main__":
    main()
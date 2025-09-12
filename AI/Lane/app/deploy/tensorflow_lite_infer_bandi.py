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
        self.num_cell_row = int(getattr(cfg, "num_cell_row", 200))
        self.num_cell_col = int(getattr(cfg, "num_cell_col", 100))
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
        coords = self.pred2coords(preds, ori_w=self.ori_img_w, ori_h=self.ori_img_h)

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

        coords = None

        return coords

def main():
    args = get_args()

    model = UFLDv2TFLite(
        model_path=args.model_path,
        config_path=args.config_path,
        ori_size=args.ori_size,
        debug=args.debug,
        num_threads=args.threads,
    )

    path = args.input_path
    ext = os.path.splittext(path)[1].lower()

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

if __name__ == "__main__":
    main()
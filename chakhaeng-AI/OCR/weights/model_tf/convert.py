# convert.py
import argparse, glob, os
import numpy as np
from typing import Iterator, List
#from PIL import Image
import tensorflow as tf

def rep_data_gen(img_dir: str, input_h: int, input_w: int, max_imgs: int = 200):
    paths: List[str] = sorted(glob.glob(os.path.join(img_dir, "*")))
    count = 0
    for p in paths:
        try:
            img = Image.open(p).convert("RGB").resize((input_w, input_h))
            arr = np.asarray(img, dtype=np.float32) / 255.0
            arr = np.expand_dims(arr, 0)  # [1,H,W,3]
            yield [arr]
            count += 1
            if count >= max_imgs:
                break
        except Exception:
            continue

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--saved_model_dir", required=True)
    ap.add_argument("--mode", choices=["dynamic","fp16","int8"], default="dynamic")
    ap.add_argument("--output", default="model.tflite")
    ap.add_argument("--input_hw", nargs=2, type=int, metavar=("H","W"), required=False,
                    help="INT8용 대표데이터 전처리에 사용(H W). ex) --input_hw 320 1600")
    ap.add_argument("--rep_data_dir", type=str, help="INT8 대표데이터 폴더")
    ap.add_argument("--int8_io", action="store_true", help="입출력까지 int8로 고정")
    args = ap.parse_args()

    converter = tf.lite.TFLiteConverter.from_saved_model(args.saved_model_dir)

    if args.mode == "dynamic":
        converter.optimizations = [tf.lite.Optimize.DEFAULT]

    elif args.mode == "fp16":
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        converter.target_spec.supported_types = [tf.float16]

    elif args.mode == "int8":
        if not args.rep_data_dir or not args.input_hw:
            raise ValueError("INT8은 --rep_data_dir 와 --input_hw(H W)가 필요합니다.")
        H, W = args.input_hw
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        converter.representative_dataset = lambda: rep_data_gen(args.rep_data_dir, H, W)
        converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
        if args.int8_io:
            converter.inference_input_type  = tf.int8
            converter.inference_output_type = tf.int8

    tflite_model = converter.convert()
    with open(args.output, "wb") as f:
        f.write(tflite_model)
    print(f"✅ Saved: {args.output}")

if __name__ == "__main__":
    main()

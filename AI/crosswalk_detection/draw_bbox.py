# save as: draw_yolo_separate_dirs.py
import cv2
from pathlib import Path
import argparse

IMG_EXTS = {".jpg", ".jpeg", ".png", ".bmp", ".tif", ".tiff"}

def color_for(cls_id: int):
    palette = [
        (255, 0, 0), (0, 255, 0), (0, 0, 255),
        (255, 255, 0), (255, 0, 255), (0, 255, 255),
        (128, 0, 255), (255, 128, 0), (0, 128, 255)
    ]
    return palette[cls_id % len(palette)]

def parse_class_names(path: str):
    if not path:
        return None
    p = Path(path)
    if not p.exists():
        print(f"[WARN] 클래스 파일 없음: {p}")
        return None
    names = {}
    with open(p, "r", encoding="utf-8") as f:
        for i, line in enumerate(f):
            name = line.strip()
            if name:
                names[i] = name
    return names

def draw_one(img_path: Path, label_path: Path, out_path: Path, class_names=None, thickness=2):
    img = cv2.imread(str(img_path))
    if img is None:
        print(f"[WARN] 이미지 로드 실패: {img_path}")
        return False
    H, W = img.shape[:2]

    if not label_path.exists():
        print(f"[INFO] 라벨 없음, 스킵: {label_path}")
        return False

    with open(label_path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            parts = line.split()
            if len(parts) < 5:
                print(f"[WARN] 잘못된 라벨: {label_path} :: {line}")
                continue
            try:
                cls = int(float(parts[0]))
                xc, yc, w, h = map(float, parts[1:5])
            except ValueError:
                print(f"[WARN] 파싱 실패: {label_path} :: {line}")
                continue

            bx, by = xc * W, yc * H
            bw, bh = w * W, h * H
            x1 = int(round(bx - bw/2)); y1 = int(round(by - bh/2))
            x2 = int(round(bx + bw/2)); y2 = int(round(by + bh/2))

            x1 = max(0, min(W-1, x1)); y1 = max(0, min(H-1, y1))
            x2 = max(0, min(W-1, x2)); y2 = max(0, min(H-1, y2))

            color = color_for(cls)
            cv2.rectangle(img, (x1, y1), (x2, y2), color, thickness)

            label_txt = str(class_names.get(cls, cls)) if class_names else str(cls)
            (tw, th), baseline = cv2.getTextSize(label_txt, cv2.FONT_HERSHEY_SIMPLEX, 0.5, 1)
            y_text = max(0, y1-5)
            cv2.rectangle(img, (x1, y_text-th-2), (x1+tw+2, y_text+baseline), color, -1)
            cv2.putText(img, label_txt, (x1+1, y_text-2), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0,0,0), 1, cv2.LINE_AA)

    out_path.parent.mkdir(parents=True, exist_ok=True)
    cv2.imwrite(str(out_path), img)
    return True

def process_split(images_root: Path, labels_root: Path, out_root: Path, split: str, class_names=None, thickness=2):
    img_dir = images_root / split
    lbl_dir = labels_root / split
    out_dir = out_root / split

    if not img_dir.exists():
        print(f"[ERR] 이미지 폴더 없음: {img_dir}")
        return

    total, drawn = 0, 0
    for img_path in img_dir.rglob("*"):
        if img_path.suffix.lower() not in IMG_EXTS:
            continue
        total += 1

        # labels/<split>/<relative_path>.txt  (확장자만 .txt로 변경)
        rel = img_path.relative_to(img_dir)
        label_path = (lbl_dir / rel).with_suffix(".txt")

        # 출력: images_vis/<split>/<relative_path> *_vis.jpg
        out_path = (out_dir / rel).with_suffix("")  # 확장자 제거
        out_path = out_path.with_name(out_path.name + "_vis").with_suffix(".jpg")

        if draw_one(img_path, label_path, out_path, class_names, thickness):
            drawn += 1
            print(f"[OK] {img_path} -> {out_path}")

    print(f"[DONE] split='{split}' | 이미지 {total}장, 시각화 {drawn}장")

def main():
    ap = argparse.ArgumentParser(description="YOLO 시각화 (images/, labels/ 분리 구조)")
    ap.add_argument("--images_root", default="dataset/train/images", help="예: dataset/images")
    ap.add_argument("--labels_root", default="dataset/train/labels", help="예: dataset/labels")
    ap.add_argument("--out_root", default="dataset/train_images_vis", help="예: dataset/images_vis")
    ap.add_argument("--split", default="", help="train/val/test 등 (기본: train)")
    ap.add_argument("--classes", default=None, help="클래스 이름 파일(각 줄 하나)")
    ap.add_argument("--thickness", type=int, default=2, help="박스 두께")
    args = ap.parse_args()

    class_names = parse_class_names(args.classes)
    process_split(
        images_root=Path(args.images_root),
        labels_root=Path(args.labels_root),
        out_root=Path(args.out_root),
        split=args.split,
        class_names=class_names,
        thickness=args.thickness,
    )

if __name__ == "__main__":
    # 예:
    # python draw_yolo_separate_dirs.py --images_root dataset/images --labels_root dataset/labels --out_root dataset/images_vis --split train
    main()
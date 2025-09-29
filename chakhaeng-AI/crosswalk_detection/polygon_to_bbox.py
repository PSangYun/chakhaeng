# save as: json_poly_to_yolo_bbox.py
from pathlib import Path
import json

ROOT = Path("dataset")
JSON_DIR = ROOT / "labels"
LABEL_DIR = ROOT / "labels_bbox"
LABEL_DIR.mkdir(parents=True, exist_ok=True)

CLASS_ID = 0         # crosswalk 클래스 id
CLASS_NAME = ["crosswalk", "stop_line"]
CATEGORY_REQUIRED = ["polygon", 'polyline']  # polygon만 bbox로 변환

def to_yolo_bbox(xmin, ymin, xmax, ymax, img_w, img_h):
    xc = ((xmin + xmax) / 2.0) / img_w
    yc = ((ymin + ymax) / 2.0) / img_h
    w  = (xmax - xmin) / img_w
    h  = (ymax - ymin) / img_h
    # clamp
    xc = min(max(xc, 0.0), 1.0)
    yc = min(max(yc, 0.0), 1.0)
    w  = min(max(w , 0.0), 1.0)
    h  = min(max(h , 0.0), 1.0)
    return xc, yc, w, h

def process_one(jp: Path):
    with open(jp, "r", encoding="utf-8") as f:
        data = json.load(f)

    file_name = data["image"]["file_name"]
    img_h, img_w = data["image"]["image_size"]  # [H, W]
    anns = data.get("annotations", [])

    lines = []
    for ann in anns:
        if ann.get("class") not in CLASS_NAME:
            continue
        if ann.get("category") not in CATEGORY_REQUIRED:
            continue
        pts = ann.get("data", [])
        if not pts:
            continue
        
        xs = [p["x"] for p in pts]
        ys = [p["y"] for p in pts]
        xmin, xmax = max(0, min(xs)), min(img_w - 1, max(xs))
        ymin, ymax = max(0, min(ys)), min(img_h - 1, max(ys))
        if xmax <= xmin or ymax <= ymin:
            continue
        xc, yc, w, h = to_yolo_bbox(xmin, ymin, xmax, ymax, img_w, img_h)
        lines.append(f"{0 if ann.get('class')=='crosswalk' else 1} {xc:.6f} {yc:.6f} {w:.6f} {h:.6f}")

    # if lines:
    out_path = LABEL_DIR / (Path(file_name).stem + ".txt")
    with open(out_path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))

def main():
    jsons = sorted(JSON_DIR.glob("*.json"))
    for jp in jsons:
        process_one(jp)
    print(f"✅ YOLO bbox 라벨 생성 완료 -> {LABEL_DIR}")

if __name__ == "__main__":
    main()

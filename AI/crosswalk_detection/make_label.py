from pathlib import Path
import json

JSON_DIR = Path("dataset/labels")     # JSON 폴더
IMAGES_DIR = Path("dataset/images")  # 이미지 폴더
LABELS_DIR = Path("dataset/texts")  # YOLO 라벨 저장 폴더
LABELS_DIR.mkdir(parents=True, exist_ok=True)

CLASS_NAME = "crosswalk"
CLASS_ID = 0  # 단일 클래스라면 0

def norm_xy(x, y, w, h):
    return min(max(x / w, 0.0), 1.0), min(max(y / h, 0.0), 1.0)

def process_one(json_path: Path):
    with open(json_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    file_name = data["image"]["file_name"]
    img_h, img_w = data["image"]["image_size"]  # [H, W]
    anns = data.get("annotations", [])

    lines = []
    for ann in anns:
        if ann.get("class") != CLASS_NAME or ann.get("category") != "polygon":
            continue
        pts = ann.get("data", [])
        if not pts or len(pts) < 3:
            continue

        coords = []
        for p in pts:
            x_n, y_n = norm_xy(p["x"], p["y"], img_w, img_h)
            coords.append(f"{x_n:.6f} {y_n:.6f}")
        line = f"{CLASS_ID} " + " ".join(coords)
        lines.append(line)

    if lines:
        out_path = LABELS_DIR / (Path(file_name).stem + ".txt")
        with open(out_path, "w", encoding="utf-8") as f:
            f.write("\n".join(lines))

def main():
    for jp in JSON_DIR.glob("*.json"):
        process_one(jp)
    print("✅ crosswalk만 YOLO segmentation 라벨 변환 완료")

if __name__ == "__main__":
    main()

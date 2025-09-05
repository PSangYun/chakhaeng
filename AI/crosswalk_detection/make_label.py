import json
from pathlib import Path
import argparse
import shutil

# 유지할 클래스와 각 카테고리의 최소 점 개수 규칙
KEEP_CLASSES = {
    "crosswalk": ("polygon", 3),   # (기대 category, 최소 점 수)
    "stop_line": ("polyline", 2),
}

def is_valid_ann(ann: dict) -> bool:
    cls = ann.get("class")
    cat = ann.get("category")
    pts = ann.get("data", [])
    if cls not in KEEP_CLASSES:
        return False
    expected_cat, min_pts = KEEP_CLASSES[cls]
    if cat != expected_cat:
        return False
    if not isinstance(pts, list) or len(pts) < min_pts:
        return False
    # 좌표 키 검증
    for p in pts:
        if not isinstance(p, dict) or "x" not in p or "y" not in p:
            return False
    return True

def filter_one(in_path: Path, out_path: Path, keep_empty: bool):
    with open(in_path, "r", encoding="utf-8") as f:
        src = json.load(f)

    anns_in = src.get("annotations", [])
    kept = []
    for ann in anns_in:
        if is_valid_ann(ann):
            # 필요한 키만 정리해서 보존(원하면 attributes 등도 유지 가능)
            kept.append({
                "class": ann["class"],
                "attributes": ann.get("attributes", []),
                "category": ann["category"],
                "data": ann["data"],
            })

    if not kept and not keep_empty:
        return False

    dst = {
        "image": src.get("image", {}),
        "annotations": kept
    }
    out_path.parent.mkdir(parents=True, exist_ok=True)
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(dst, f, ensure_ascii=False, indent=2)
    return True

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--in_dir", default="dataset/labels", help="원본 JSON 폴더")
    ap.add_argument("--out_dir", default="dataset/labels_filtered", help="필터 결과 JSON 폴더")
    ap.add_argument("--keep_empty", default=True,
                    help="대상 객체가 없어도 빈 annotations로 JSON 생성")
    ap.add_argument("--copy_images", default=False,
                    help="image.file_name을 기준으로 이미지를 out_dir/images로 복사")
    ap.add_argument("--images_root", default="dataset/images",
                    help="이미지 루트 경로 (copy_images 사용할 때 필요)")
    args = ap.parse_args()

    in_dir = Path(args.in_dir)
    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    n_json = n_written = n_objs = 0

    for jp in sorted(in_dir.glob("*.json")):
        n_json += 1
        out_path = out_dir / jp.name
        ok = filter_one(jp, out_path, args.keep_empty)
        if ok:
            n_written += 1
            # 카운트
            with open(out_path, "r", encoding="utf-8") as f:
                n_objs += len(json.load(f).get("annotations", []))
            # 이미지 복사(옵션)
            if args.copy_images and args.images_root:
                try:
                    with open(jp, "r", encoding="utf-8") as f:
                        data = json.load(f)
                    img_name = data.get("image", {}).get("file_name")
                    if img_name:
                        src_img = Path(args.images_root) / img_name
                        dst_img = out_dir / "images" / Path(img_name).name
                        dst_img.parent.mkdir(parents=True, exist_ok=True)
                        if src_img.exists():
                            shutil.copy2(src_img, dst_img)
                except Exception:
                    pass

    print(f"총 JSON: {n_json}")
    print(f"생성된 결과 JSON: {n_written}")
    print(f"남은 객체 수 합계: {n_objs}")

if __name__ == "__main__":
    main()
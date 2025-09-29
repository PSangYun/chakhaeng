# single_infer.py
import os, glob, torch, cv2
from PIL import Image
import torchvision.transforms as T
from utils.common import merge_config, get_model
from demo import pred2coords  # demo.py의 함수 재사용

import argparse, sys

import time

_user_parser = argparse.ArgumentParser(add_help=False)
_user_parser.add_argument("--image", type=str, default=None, help="단일 이미지 경로")
_user_parser.add_argument("--images_dir", type=str, default=None, help="이미지 폴더 경로")
_user_args, _remaining = _user_parser.parse_known_args()

# merge_config가 --image 등의 커스텀 옵션을 몰라도 되도록 제거
sys.argv = [sys.argv[0]] + _remaining

def build_transform(cfg):
    # demo.py와 동일한 전처리: Resize(H/crop_ratio, W) -> ToTensor -> Normalize
    return T.Compose([
        T.Resize( (int(cfg.train_height / cfg.crop_ratio), cfg.train_width) ),
        T.ToTensor(),
        T.Normalize( (0.485, 0.456, 0.406), (0.229, 0.224, 0.225) ),
    ])

@torch.no_grad()
def load_model(cfg):
    net = get_model(cfg)
    sd = torch.load(cfg.test_model, map_location='cpu')['model']
    # 'module.' prefix 정리
    sd = { (k[7:] if k.startswith('module.') else k): v for k, v in sd.items() }
    net.load_state_dict(sd, strict=False)
    net.eval()
    if torch.cuda.is_available():
        net.cuda()
    return net

def infer_image(model, cfg, img_path, out_path=None):
    start_time = time.time()
    # 1) 원본 이미지 읽기 (시각화용)
    vis = cv2.imread(img_path)
    if vis is None:
        raise FileNotFoundError(f"Cannot read image: {img_path}")
    img_h, img_w = vis.shape[:2]

    # 2) 네트워크 입력 만들기 (demo.py와 동일 규칙)
    tfm = build_transform(cfg)
    pil = Image.open(img_path).convert('RGB')
    x = tfm(pil)  # [C, H_resized, W]
    # 아래쪽 crop: 최종 입력 높이는 cfg.train_height
    H = x.shape[1]
    bot = x[:, H - cfg.train_height : , :]  # [C, train_height, train_width]
    if torch.cuda.is_available():
        bot = bot.cuda().unsqueeze(0)
    else:
        bot = bot.unsqueeze(0)

    # 3) 추론
    pred = model(bot)

    # 4) 좌표 변환 (출력은 원본 해상도로 스케일해 찍을 거라 img_w/img_h 사용)
    coords = pred2coords(
        pred,
        cfg.row_anchor,
        cfg.col_anchor,
        original_image_width = img_w,
        original_image_height = img_h
    )

    print("inference_time : ", time.time() - start_time)

    # 5) 시각화
    out = vis.copy()
    for lane in coords:
        for (xpt, ypt) in lane:
            cv2.circle(out, (int(xpt), int(ypt)), 5, (0,255,0), -1)

    # 6) 저장/표시
    if out_path is None:
        base = os.path.splitext(os.path.basename(img_path))[0]
        out_path = f"{base}_lane.jpg"
    cv2.imwrite(out_path, out)
    return out_path

def infer_folder(model, cfg, folder):
    exts = ('*.jpg','*.jpeg','*.png','*.bmp')
    paths = []
    for e in exts:
        paths += glob.glob(os.path.join(folder, e))
    paths.sort()
    results = []
    for p in paths:
        out = os.path.join(folder, os.path.splitext(os.path.basename(p))[0] + "_lane.jpg")
        results.append(infer_image(model, cfg, p, out))
    return results

if __name__ == "__main__":
    # 0) 설정 로드 (cfg.test_model 경로 반드시 세팅!)
    args, cfg = merge_config()
    assert os.path.isfile(cfg.test_model), "cfg.test_model을 유효한 가중치(.pth)로 설정하세요."

    model = load_model(cfg)

    # 사용 예시:
    # 1) 단일 이미지
    #breakpoint()
    out_path = infer_image(model, cfg, _user_args.image)
    print("saved ->", out_path)

    # 2) 폴더 전체
    # outs = infer_folder(model, cfg, "./images")
    # print("\n".join(outs))

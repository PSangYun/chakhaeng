# -*- coding: utf-8 -*-

"""
@Time    : 2025/4/26 15:43
@File    : plate_dataset.py
@Author  : zj
@Description: 
한국 차 번호판 데이터셋을 위한 데이터 로더
"""

import os
import re

from PIL import Image
from pathlib import Path
from torch.utils.data import Dataset

RANK = int(os.getenv('RANK', -1))

DELIMITER = '_'

# 한국 번호판에 사용되는 문자 집합 정의
# 한글 자모는 초성/중성/종성 조합이 아닌, 완성된 글자 형태를 사용해야 합니다.
KOREAN_CHARS = "가나다라마바사아자차카타파하거너더러머버서어저허고노도로모보소오조호구누두루무부수우주후"
NUMBERS_AND_ALPHAS = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ"
PLATE_CHARS = KOREAN_CHARS + NUMBERS_AND_ALPHAS

PLATE_DICT = {char: i for i, char in enumerate(PLATE_CHARS)}


def load_data(data_root, pattern='*.jpg'):
    assert os.path.isdir(data_root)

    data_list = list()

    p = Path(data_root)
    for path in p.rglob(pattern):
        data_list.append(str(path).strip())

    return data_list


def is_plate_right(plate_name):
    """
    번호판 이름이 유효한 문자로만 구성되었는지 확인
    """
    assert isinstance(plate_name, str), plate_name
    for ch in plate_name:
        if ch not in PLATE_CHARS:
            return False
    return True


def create_plate_label(img_list):
    """
    이미지 경로로부터 번호판 레이블을 추출하고 데이터 리스트를 생성
    """
    data_list = list()
    label_dict = dict()
    for img_path in img_list:
        assert os.path.isfile(img_path), img_path
        
        # 파일명에서 번호판 레이블 추출
        # 파일명 형식: "번호판-일련번호.jpg" 또는 "번호판_일련번호.jpg"
        # 예시: 25가1234_1.jpg
        img_name = os.path.splitext(os.path.basename(img_path))[0]
        label_name = img_name.split(DELIMITER)[0]
        
        # 유효성 검사
        # 한국 번호판은 보통 7~8자이므로, 길이를 기준으로 필터링할 수 있습니다.
        # (예: 2자리 숫자-한글-3자리 숫자, 2자리 숫자-한글-4자리 숫자 등)
        # 여기서는 간단하게 공백, 짧은 길이, 유효하지 않은 문자만 검사합니다.
        if " " in label_name:
            continue
        if len(label_name) < 3:  # 최소 길이 설정 (예: 서울3456)
            continue
        if not is_plate_right(label_name):
            continue

        if label_name not in label_dict.keys():
            label = [PLATE_DICT[char] for char in label_name]
            label_dict[label_name] = label
        
        data_list.append([img_path, label_name])

    return data_list, label_dict


class PlateDataset(Dataset):
    """
    한국 차 번호판 데이터셋 클래스
    """
    def __init__(self, data_root, is_train=True):
        self.data_root = data_root
        self.is_train = is_train

        dir_name = '자동차 차종-연식-번호판 인식용 영상\\Training' if is_train else '자동차 차종-연식-번호판 인식용 영상\\Validation'
        data_dir = os.path.join(data_root, dir_name)
        
        assert os.path.isdir(data_dir), f"경로를 찾을 수 없습니다: {data_dir}"
        
        img_list = load_data(data_dir, pattern="*.jpg")
        assert len(img_list) > 0, "데이터셋에서 이미지를 찾을 수 없습니다."
        
        self.data_list, self.label_dict = create_plate_label(img_list)
        if RANK in {-1, 0}:
            print(f"로드된 {'훈련' if is_train else '테스트'} 데이터: {len(self.data_list)}개")

        self.dataset_len = len(self.data_list)

    def __getitem__(self, index):
        assert index < self.dataset_len

        img_path, label_name = self.data_list[index]
        image = Image.open(img_path)

        return image, label_name, img_path

    def __len__(self):
        return self.dataset_len


if __name__ == '__main__':
    # 예시 실행 코드
    # 실제 데이터셋 경로로 바꿔주세요.
    data_root = "C:\\ssafy\\S13P21D202\\AI\\OCR\\dataset"
    val_dataset = PlateDataset(data_root, is_train=False)
    print(val_dataset)

    image, label_name, img_path = val_dataset.__getitem__(100)
    print(f"이미지 크기: {image.size}")
    print(f"레이블: {label_name}")
    print(f"이미지 경로: {img_path}")
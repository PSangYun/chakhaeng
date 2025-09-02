# -*- coding: utf-8 -*-

"""
@Time : 2025/4/26 15:43
@File : plate_dataset.py
@Author : zj
@Description: 
한국 차 번호판 데이터셋을 위한 데이터 로더
"""

import os
import random
from pathlib import Path
import json
import cv2
import numpy as np
import torch
from torch.utils.data import Dataset
from torchvision import transforms

RANK = int(os.getenv('RANK', -1))

DELIMITER = '_'

# 한국 번호판에 사용되는 문자 집합 정의
# 한글 자모는 초성/중성/종성 조합이 아닌, 완성된 글자 형태를 사용해야 합니다.
KOREAN_CHARS = "가나다라마거너더러머서어저고노도로모보소오조구누두루무부수우주아바사자허하호배"
REGION_CHARS = "경기충북남전천울금영용산인평미추홀"
NUMBERS = "0123456789"
PLATE_CHARS = KOREAN_CHARS + REGION_CHARS + NUMBERS

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


def create_plate_label(img_list, label_dir):
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
        #label_filename = img_name.replace(".jpg", ".json")
        label_filename = img_name + ".json"
        label_path = os.path.join(label_dir, label_filename)

        # JSON 파일이 존재하는지 확인
        if not os.path.isfile(label_path):
            print(f"경고: 레이블 파일이 없습니다: {label_path}")
            continue

        try:
            with open(label_path, 'r', encoding='utf-8') as f:
                label_data = json.load(f)
                label_name = label_data.get('value', None)
        except (json.JSONDecodeError, FileNotFoundError) as e:
            print(f"경고: 레이블 파일을 읽는 중 오류 발생 ({e}): {label_path}")
            continue

        if not label_name:
            continue
        
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
    def __init__(self, data_root, is_train=True, input_shape=(160, 48)):
        self.data_root = data_root
        self.is_train = is_train
        self.input_shape = input_shape

        # 새로운 데이터셋 구조에 맞게 경로 설정
        # 이미지와 레이블 디렉토리 경로 설정
        if is_train:
            image_dir = os.path.join(data_root, 'plate_data', 'Training', 'images')
            label_dir = os.path.join(data_root, 'plate_data', 'Training', 'labels')
        else:
            image_dir = os.path.join(data_root, 'plate_data', 'Validation', 'images')
            label_dir = os.path.join(data_root, 'plate_data', 'Validation', 'labels')
        
        assert os.path.isdir(image_dir), f"이미지 경로를 찾을 수 없습니다: {image_dir}"
        assert os.path.isdir(label_dir), f"레이블 경로를 찾을 수 없습니다: {label_dir}"

        img_list = load_data(image_dir, pattern="*.jpg")
        assert len(img_list) > 0, "데이터셋에서 이미지를 찾을 수 없습니다."
        
        self.data_list, self.label_dict = create_plate_label(img_list, label_dir)
        if RANK in {-1, 0}:
            print(f"로드된 {'훈련' if is_train else '테스트'} 데이터: {len(self.data_list)}개")

        self.dataset_len = len(self.data_list)
        self.transform = transforms.Compose([
            transforms.ToPILImage(),
            transforms.RandomRotation(15, fill=0),
            transforms.RandomAffine(degrees=5, translate=(0.1, 0.1), scale=(0.9, 1.1)),
            transforms.ColorJitter(brightness=0.2, contrast=0.2, saturation=0.2, hue=0.1),
        ])

    def __getitem__(self, index):
        assert index < self.dataset_len

        img_path, label_name = self.data_list[index]
        #image = cv2.imread(img_path)
        image = cv2.imdecode(np.fromfile(img_path, dtype=np.uint8), cv2.IMREAD_UNCHANGED)
        
        if image.shape[-1] == 4:
            image = cv2.cvtColor(image, cv2.COLOR_BGRA2BGR)

        if self.is_train and random.random() > 0.5:
            image = self.transform(image)
            image = np.array(image, dtype=np.uint8)
        image = cv2.resize(image, self.input_shape)
        
        data = torch.from_numpy(image).float() / 255.
        # HWC -> CHW
        data = data.permute(2, 0, 1)
        return data, label_name#, img_path

    def __len__(self):
        return self.dataset_len
    
    def convert(self, targets):
        labels = []
        for label_name in targets:
            label = self.label_dict[label_name]
            labels.append(torch.IntTensor(label))
        return labels


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
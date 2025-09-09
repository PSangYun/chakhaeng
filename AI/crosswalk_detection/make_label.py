import os
import shutil
import random

# 경로 설정
img_dir = "dataset/images"
label_dir = "dataset/labels_bbox"

# 결과 저장할 경로
train_img_dir = "dataset/train/images"
train_label_dir = "dataset/train/labels"
test_img_dir = "dataset/test/images"
test_label_dir = "dataset/test/labels"

os.makedirs(train_img_dir, exist_ok=True)
os.makedirs(train_label_dir, exist_ok=True)
os.makedirs(test_img_dir, exist_ok=True)
os.makedirs(test_label_dir, exist_ok=True)

# label 파일 목록 불러오기
label_files = [f for f in os.listdir(label_dir) if f.endswith(".txt")]

non_empty = []
empty = []

for lf in label_files:
    with open(os.path.join(label_dir, lf), "r") as f:
        content = f.read().strip()
        if content:
            non_empty.append(lf)   # 객체 있는 경우
        else:
            empty.append(lf)       # 객체 없는 경우

print(f"객체 있는 이미지 수: {len(non_empty)}")
print(f"객체 없는 이미지 수: {len(empty)}")

# 조건 적용
selected_empty = random.sample(empty, 10000)
final_files = non_empty + selected_empty

# train:test = 10:1 split
random.shuffle(final_files)
train_size = int(len(final_files) * (10/11))
train_files = final_files[:train_size]
test_files = final_files[train_size:]

def copy_files(file_list, dst_img_dir, dst_label_dir):
    for lf in file_list:
        img_name = os.path.splitext(lf)[0] + ".jpg"  # 이미지 확장자 맞춰서 변경 (jpg/png 확인 필요)
        shutil.copy(os.path.join(img_dir, img_name), os.path.join(dst_img_dir, img_name))
        shutil.copy(os.path.join(label_dir, lf), os.path.join(dst_label_dir, lf))

copy_files(train_files, train_img_dir, train_label_dir)
copy_files(test_files, test_img_dir, test_label_dir)

print("데이터셋 분할 완료!")

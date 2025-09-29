# Dockerfile (GPU)
FROM nvidia/cuda:12.1.1-cudnn8-runtime-ubuntu22.04

ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3.10 python3-pip python3-venv git ffmpeg libgl1 ca-certificates vim wget && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 의존성 설치
COPY requirements.txt /tmp/requirements.txt
RUN python3 -m pip install --upgrade pip && \
    pip install -r /tmp/requirements.txt

# PyTorch + CUDA 12.1
RUN pip install torch==2.3.1 torchvision==0.18.1 --index-url https://download.pytorch.org/whl/cu121
RUN pip install numpy==1.23.0 nvidia-dali-cuda120

# 소스 복사
COPY . /app
ENV PYTHONUNBUFFERED=1 \
    PYTHONPATH=/app

CMD ["/bin/bash"]
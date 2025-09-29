import onnx

model = onnx.load("before_crnn_cpu.onnx")
onnx.save(model, "crnn_cpu.onnx")  # 그대로 저장하면 버전 안 바뀜

# opset version 낮추기 위해 optimizer 이용
from onnx import version_converter, helper

converted_model = version_converter.convert_version(model, 11)  # 예: opset 11로 변환
onnx.save(converted_model, "model_op11.onnx")
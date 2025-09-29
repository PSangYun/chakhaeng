import onnx
from onnx import helper

model = onnx.load("crnn_final.onnx")

for node in model.graph.node:
    if node.op_type == "GRU":
        found = False
        for attr in node.attribute:
            if attr.name == "activations":
                found = True
                # 강제로 4개 기본값 설정
                attr.strings[:] = [b"Sigmoid", b"Tanh", b"Sigmoid", b"Tanh"]
        if not found:
            # 아예 없으면 새로 추가
            node.attribute.extend([
                helper.make_attribute("activations", ["Sigmoid", "Tanh", "Sigmoid", "Tanh"])
            ])

onnx.save(model, "crnn_real_final.onnx")
import onnx

model = onnx.load("crnn_real_final.onnx")

for node in model.graph.node:
    if node.op_type == "GRU":
        print("GRU Node:", node.name)
        for attr in node.attribute:
            print(attr)
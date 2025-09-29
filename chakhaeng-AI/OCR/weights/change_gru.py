import onnx

model = onnx.load("crnn_cpu_simplified_op11.onnx")

for node in model.graph.node:
    if node.op_type == "GRU":
        for attr in node.attribute:
            if attr.name == "linear_before_reset":
                print("Before:", attr.i)
                attr.i = 0
                print("After:", attr.i)

onnx.save(model, "crnn_final.onnx")
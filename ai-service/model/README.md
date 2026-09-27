# CityCare AI Model Directory

This directory is intended to store pre-trained weights for Civic Issue Classification:

- Supported formats:
  - YOLOv8 / YOLOv11 (`best.pt`, `best.onnx`) for object detection (potholes, garbage piles, manholes)
  - PyTorch / TorchScript (`model.pt`)
  - TensorFlow / Keras (`saved_model/` or `model.h5`)
  - ONNX Runtime (`classifier.onnx`)

## Status
When no trained weights are present in this folder, `ai-service/app.py` automatically runs in **Transparent Heuristic / Prototype Fallback Mode**.
- It clearly signals `"is_trained_model": false` and `"engine": "heuristic_fallback_v1"`.
- It performs color-space & edge analysis with Pillow, plus text/filename context matching, to calculate realistic categories and priorities without requiring paid APIs or GPU hardware.

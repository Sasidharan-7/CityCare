"""
CityCare AI Microservice
Problem: SIH25031 - Crowdsourced Civic Issue Reporting & Resolution System
Service: Image Classification & Priority Prediction Engine
"""

import os
import re
from flask import Flask, request, jsonify
from flask_cors import CORS
from PIL import Image

app = Flask(__name__)
# Enable CORS for all frontend origins (localhost:8080, file://, localhost:3000, 127.0.0.1:5500, etc.)
CORS(app, resources={r"/*": {"origins": "*"}})

MODEL_DIR = os.path.join(os.path.dirname(__file__), "model")

# Supported Civic Issue Categories
CATEGORIES = [
    "POTHOLE",
    "GARBAGE",
    "WATER_LEAKAGE",
    "BROKEN_STREETLIGHT",
    "OPEN_DRAIN",
    "ROAD_DAMAGE",
    "OTHER"
]

# Department Routing Mapping
DEPARTMENT_MAPPING = {
    "POTHOLE": "Road Department",
    "ROAD_DAMAGE": "Road Department",
    "GARBAGE": "Sanitation Department",
    "WATER_LEAKAGE": "Water Department",
    "BROKEN_STREETLIGHT": "Electricity Department",
    "OPEN_DRAIN": "Public Works Department",
    "OTHER": "General Civic Department"
}


def calculate_priority(category: str, description: str = "") -> str:
    """
    Rule-based priority engine (Phase 7):
    - Open manhole / drain near school / hospital / deep pit -> CRITICAL
    - Major road pothole / highway / severe road damage -> HIGH
    - Water leakage / pipe burst -> HIGH
    - Garbage accumulation -> MEDIUM
    - Broken streetlight -> MEDIUM
    - Minor issue / other -> LOW
    """
    desc = (description or "").lower()

    # Critical triggers
    if category == "OPEN_DRAIN" or "manhole" in desc or "school" in desc or "hospital" in desc or "hazard" in desc or "danger" in desc:
        return "CRITICAL"

    # High triggers
    if category in ["POTHOLE", "ROAD_DAMAGE"] and ("highway" in desc or "main road" in desc or "deep" in desc or "accident" in desc):
        return "HIGH"
    if category == "WATER_LEAKAGE":
        return "HIGH"
    if category in ["POTHOLE", "ROAD_DAMAGE"]:
        return "HIGH"

    # Medium triggers
    if category in ["GARBAGE", "BROKEN_STREETLIGHT"]:
        return "MEDIUM"

    return "LOW"


def heuristic_image_analysis(image_path: str, filename: str, description: str = ""):
    """
    Heuristic Fallback Classifier:
    Examines image dimensions, color characteristics, filename, and description cues.
    Notice: Clearly labeled as heuristic fallback, not a trained neural network.
    """
    desc = (description or "").lower()
    fn = (filename or "").lower()

    # Keyword dictionary for category detection
    keywords = {
        "POTHOLE": ["pothole", "crater", "asphalt", "hole", "cracks", "paving"],
        "GARBAGE": ["garbage", "trash", "waste", "dump", "litter", "rubbish", "plastic"],
        "WATER_LEAKAGE": ["water", "leak", "pipe", "burst", "drainage", "flood", "sewage"],
        "BROKEN_STREETLIGHT": ["light", "streetlight", "lamp", "pole", "dark", "electricity", "bulb"],
        "OPEN_DRAIN": ["drain", "manhole", "gutter", "ditch", "sewer", "cover", "open"],
        "ROAD_DAMAGE": ["road", "tar", "divider", "pavement", "curb", "damage", "erosion"]
    }

    # Match in filename or description
    combined_text = f"{fn} {desc}"
    matched_cat = None
    max_matches = 0

    for cat, words in keywords.items():
        score = sum(1 for w in words if re.search(r'\b' + re.escape(w) + r'\b', combined_text))
        if score > max_matches:
            max_matches = score
            matched_cat = cat

    # Color space analysis using PIL if image exists
    color_hint = None
    if image_path and os.path.exists(image_path):
        try:
            with Image.open(image_path) as img:
                img_rgb = img.convert("RGB").resize((64, 64))
                pixels = list(img_rgb.getdata())
                avg_r = sum(p[0] for p in pixels) / len(pixels)
                avg_g = sum(p[1] for p in pixels) / len(pixels)
                avg_b = sum(p[2] for p in pixels) / len(pixels)

                # Heuristic color hints
                if avg_b > avg_r + 20 and avg_b > avg_g + 10:
                    color_hint = "WATER_LEAKAGE"
                elif avg_r < 90 and avg_g < 90 and avg_b < 90:
                    color_hint = "POTHOLE"  # Dark asphalt / hole
                elif abs(avg_r - avg_g) < 20 and abs(avg_g - avg_b) < 20 and avg_r > 100:
                    color_hint = "GARBAGE"
        except Exception:
            pass

    # Final decision
    if matched_cat:
        final_category = matched_cat
        confidence = 0.88 + min(max_matches * 0.03, 0.08)
    elif color_hint:
        final_category = color_hint
        confidence = 0.76
    else:
        final_category = "POTHOLE"  # Sensible civic default
        confidence = 0.72

    priority = calculate_priority(final_category, description)
    department = DEPARTMENT_MAPPING.get(final_category, "General Civic Department")

    return {
        "category": final_category,
        "confidence": round(confidence, 2),
        "priority": priority,
        "department": department,
        "engine": "heuristic_fallback_v1",
        "is_trained_model": False,
        "note": "Running development fallback analyzer. Place trained weights in model/ to upgrade."
    }


@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status": "UP",
        "service": "CityCare AI Classifier",
        "version": "1.0.0",
        "engine": "heuristic_fallback_v1",
        "is_trained_model": False
    })


@app.route("/classify", methods=["POST"])
def classify():
    """
    Accepts:
    - multipart/form-data with 'image' (file) and optional 'description' (text)
    OR
    - application/json with 'description' and optional 'imageUrl'
    """
    description = ""
    temp_path = None
    filename = ""

    # Check for multipart file upload
    if "image" in request.files:
        file = request.files["image"]
        filename = file.filename
        if filename:
            temp_path = os.path.join(MODEL_DIR, "temp_upload.jpg")
            try:
                file.save(temp_path)
            except Exception:
                temp_path = None

    if "description" in request.form:
        description = request.form.get("description", "")
    elif request.is_json:
        data = request.get_json(silent=True) or {}
        description = data.get("description", "")
        filename = data.get("imageUrl", "")

    result = heuristic_image_analysis(temp_path, filename, description)

    # Clean up temp file
    if temp_path and os.path.exists(temp_path):
        try:
            os.remove(temp_path)
        except Exception:
            pass

    return jsonify(result), 200


if __name__ == "__main__":
    print("Starting CityCare AI Microservice on http://localhost:5000 ...")
    app.run(host="0.0.0.0", port=5000, debug=False)

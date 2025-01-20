from flask import request, jsonify


def register_handlers(app):
    @app.route("/process", methods=["POST"])
    def process():
        data = request.json
        input_text = data.get("input")
        # Call the LLM model
        output_text = "AI says: " + input_text  # Placeholder for now
        return jsonify({"output": output_text})

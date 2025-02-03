from flask import request, jsonify, Response, stream_with_context
from llms.llm import LLM


def register_handlers(app):

    @app.route("/chat", methods=["POST"])
    def chat():
        # Get and pack input
        data = request.json
        input_text = data.get("input")
        if not input_text:
            return jsonify({"error": "Input text is required"}), 400

        # Initialize LLM and stream response
        llm = LLM()
        messages = [
            {"role": "system", "content": "You are a minecraft villager. You are focused on living your own life. Your responses are always as short as possible."},
            {"role": "user", "content": f"{input_text}"}
        ]
        return Response(stream_with_context(llm.chat(messages, stream=True)), content_type="text/plain")


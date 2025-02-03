from ollama import Client


class LLM_local:
    """Ollama LLM interface"""

    def __init__(self, model: str = "deepseek-r1:1.5b", host: str = "http://localhost:11434", context: str = None):
        """
        Initialize the LLM with a model, host, and optional context.

        Args:
            model: The name of the model to use (default: "deepseek-r1:1.5b").
            host: The URL of the Ollama server (default: "http://localhost:11434").
            context: A system message to set the context for the conversation (default: None).
        """
        self.client = Client(host=host)
        self.model = model
        self.context = context
        self.messages = []

        # Add context as a system message if provided
        if self.context:
            self.messages.append({"role": "system", "content": self.context})

    def chat(self, prompt: str):
        """
        Send a prompt and get the response.

        Args:
            prompt: The user's input prompt.

        Returns:
            A generator that yields response chunks if streaming is enabled.
        """
        # Add the user's prompt to the messages
        self.messages.append({"role": "user", "content": prompt})

        # Get the response from the model
        response = self.client.chat(
            model=self.model,
            messages=self.messages,
            stream=True,
        )

        # Collect the assistant's response and add it to the messages
        full_response = ""
        for chunk in response:
            content = chunk["message"]["content"]
            full_response += content
            yield content

        # Add the assistant's response to the messages
        self.messages.append({"role": "assistant", "content": full_response})


# Usage example
if __name__ == "__main__":
    # Initialize with context
    context = """
    You are playing a Minecraft villager focused on living your own life. Your speech is concise.
    """
    llm = LLM_local(context=context, model="deepseek-r1:7b")

    # Chat with the model
    res = llm.chat("Who are you?")
    for chunk in res:
        print(chunk, end="", flush=True)

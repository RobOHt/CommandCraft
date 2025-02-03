import os
from openai import OpenAI


class LLM:
    """
    A Python class for interacting with DeepSeek-V3's API.
    """

    def __init__(self, base_url: str = "https://api.deepseek.com"):
        """
        Initialize the DeepSeek API client.

        Args:
            base_url (str, optional): The base URL for the DeepSeek API. Defaults to "https://api.deepseek.com".
        """
        api_key = os.getenv("DEEPSEEK_API_KEY")
        if not api_key:
            raise ValueError(
                "The environment variable `DEEPSEEK_API_KEY` is not set. "
                "To set it permanently:\n"
                "Linux/macOS: Add `export DEEPSEEK_API_KEY='your_api_key'` to your `~/.bashrc` or `~/.zshrc` file.\n"
                "Windows: Use `setx DEEPSEEK_API_KEY 'your_api_key'` in Command Prompt.\n"
                "Then restart your terminal or IDE."
            )
        self.client = OpenAI(api_key=api_key, base_url=base_url)

    def chat(self, messages: list, model: str = "deepseek-chat", stream: bool = False):
        """
        Send a chat request to the DeepSeek-V3 API.

        Args:
            messages (list): A list of message dictionaries, each containing "role" and "content".
                            Example: [
                                {"role": "system", "content": "You are a helpful assistant."},
                                {"role": "user", "content": "Hello!"}
                            ]
            model (str, optional): The model to use. Defaults to "deepseek-chat".
            stream (bool, optional): Whether to use streaming mode. Defaults to False.

        Returns:
            If stream=False: The assistant's response as a string.
            If stream=True: A generator yielding chunks of the assistant's response.
        """
        try:
            response = self.client.chat.completions.create(
                model=model,
                messages=messages,
                stream=stream
            )

            if stream:
                # Yield chunks for streaming responses
                for chunk in response:
                    if chunk.choices[0].delta.content:
                        yield chunk.choices[0].delta.content
            else:
                # Return the full response for non-streaming mode
                return response.choices[0].message.content

        except Exception as e:
            print(f"API request failed: {str(e)}")
            return None


# Example usage
if __name__ == "__main__":
    # Initialize the LLM client
    llm = LLM()

    # Example chat request
    messages = [
        {"role": "system", "content": "You are a minecraft villager. You are focused on living your own life."},
        {"role": "user", "content": "Hello!"}
    ]

    # Streaming response
    print("Assistant (streaming):", end=" ", flush=True)
    for chunk in llm.chat(messages, stream=True):
        print(chunk, end="", flush=True)

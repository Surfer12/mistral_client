from requests import post
# Removed unused openai import to avoid importing entire library
from dotenv import load_dotenv

load_dotenv()

PERPLEXITY_API_KEY = os.getenv("PERPLEXITY_API_KEY")

url = "https://api.perplexity.ai/chat/completions"

payload = {
    "model": "sonar-deep-research",
    "messages": [
        {"role": "user", "content": "Provide an in-depth analysis of the impact of AI on global job markets over the next decade."}
    ],
    "max_tokens": 500
}
headers = {
    "Authorization": "Bearer <Perplexity API Key>",
    "Content-Type": "application/json"
}

response = post(url, json=payload, headers=headers)
print(response.json())
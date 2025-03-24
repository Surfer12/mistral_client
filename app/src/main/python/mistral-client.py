import os
from mistralai import Mistral
from dotenv import load_dotenv

load_dotenv()

api_key = os.environ["MISTRAL_API_KEY"]

client = Mistral(api_key=api_key)

chat_response = client.agents.complete(
    agent_id="ag:5ebb45cf:20250324:saba-saba:7d0015ab",
    messages=[
        {
            "role": "user",
            "content": "What is the best French cheese?",
        },
    ],
)
print(chat_response.choices[0].message.content)

from flask import Flask, request, jsonify
import os
from mistralai import Mistral
from dotenv import load_dotenv

load_dotenv()  

app = Flask(__name__)

# Initialize Mistral client
api_key = os.environ.get("MISTRAL_API_KEY")
client = Mistral(api_key=api_key) if api_key else None

@app.route('/v1/cognitive/analyze', methods=['POST'])
def analyze():
    if not client:
        return jsonify({"error": "MISTRAL_API_KEY not set"}), 500
        
    try:
        data = request.get_json()
        
        # Validate input
        if not data or 'nodes' not in data:
            return jsonify({"error": "Invalid request format"}), 400
            
        # Process nodes with Mistral AI
        processed_nodes = []
        for node in data['nodes']:
            # Add cognitive analysis using Mistral's chat completion
            chat_response = client.chat.complete(
                model="mistral-saba-2502",  # or other appropriate model
                messages=[{
                    "role": "user",
                    "content": node.get('content', '')
                }]
            )
            
            # Extract analysis from response
            node['analysis'] = chat_response.choices[0].message.content
            node['processed'] = True
            processed_nodes.append(node)
            
        return jsonify({
            "success": True,
            "processed_nodes": processed_nodes
        })
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 8080))
    app.run(host='0.0.0.0', port=port) 
from prompt import prompt_manager
from adapters.openai_client import call_openai

class TravelScamClassifier:
        
    # text가 travel scam 관련 글이라면 1, 아니면 0을 반환한다
    def classify(self, text: str) -> bool:
        prompt = prompt_manager.generate_classification_prompt(text)
        output_text = call_openai(prompt, temperature=0.0)
        
        if not output_text:  # None, "" 방어
            return False

        # 안전장치 -> 숫자 이외가 섞여오면 첫 번째 '1' 또는 '0'만 취함
        if "1" in output_text and "0" in output_text:
            return output_text.index("1") < output_text.index("0")
        if "1" in output_text:
            return True
        if "0" in output_text:
            return False
        return False

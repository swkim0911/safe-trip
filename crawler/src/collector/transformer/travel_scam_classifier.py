from prompt import prompt_manager
from adapters.openai_client import call_openai

class TravelScamClassifier:
        
    # text가 tavel scam 관련 글이라면 1, 아니면 0을 반환한다
    def classify(text: str) -> bool:
        prompt = prompt_manager.generate_classification_prompt(text)
        out = call_openai(prompt, temperature=0.0)

        # 안전장치 -> 숫자 이외가 섞여오면 첫 번째 '1' 또는 '0'만 취함
        if "1" in out and "0" in out:
            return True if out.index("1") < out.index("0") else False
        if "1" in out:
            return True
        if "0" in out:
            return False
        return False

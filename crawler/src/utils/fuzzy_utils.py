from rapidfuzz import fuzz

def get_fuzz_ratio(text, target):
    return fuzz.ratio(text, target)

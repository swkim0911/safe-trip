import sys
from pathlib import Path

# 프로젝트 루트 추가
project_root = Path(__file__).parent.parent.parent / "src"
sys.path.insert(0, str(project_root))

import os
import json
from repository.reddit_repository import RedditRepository
from adapters import mongo_client
from config.etl_config import ETLConfig
from collector.transformer.travel_scam_parser import TravelScamParser

def test_single_parsing(reddit_id: str):
    """
    특정 reddit_id의 문서를 파싱하여 결과를 출력합니다.
    
    Args:
        reddit_id: 파싱할 reddit 문서의 ID
    """
    # 초기화
    etl_config = ETLConfig()
    reddit_repository = RedditRepository(
        mongo_client.get_raw_collection(),
        mongo_client.get_parsed_collection(),
        mongo_client.get_batch_job_collection(),
        etl_config
    )
    parser = TravelScamParser(etl_config)
    
    # reddit_id로 문서 조회
    document = reddit_repository.raw_collection.find_one({"reddit_id": reddit_id})
    
    if not document:
        print(f"❌ Document not found: {reddit_id}")
        return
    
    print("=" * 80)
    print(f"📄 Document Found: {reddit_id}")
    print("=" * 80)
    print(f"Title: {document.get('title', 'N/A')}")
    print(f"Subreddit: {document.get('subreddit', 'N/A')}")
    print(f"Classification: {document.get('classification', {}).get('is_travel_scam', 'N/A')}")
    print(f"Body length: {len(document.get('body', ''))} characters")
    print()
    print("-" * 80)
    print("Body:")
    print("-" * 80)
    print(document.get('body', 'N/A'))
    print("-" * 80)
    print()
    
    # 파싱 실행
    print("🔄 Parsing...")
    try:
        result = parser.parse(document['body'])
        
        print("=" * 80)
        print("✅ Parsing Result")
        print("=" * 80)
        print(json.dumps(result, indent=2, ensure_ascii=False))
        print()
        
        # 결과 분석
        if isinstance(result, list) and len(result) > 0:
            parsed = result[0]
            print("=" * 80)
            print("📊 Parsed Fields")
            print("=" * 80)
            print(f"Title:    {parsed.get('title', 'N/A')}")
            print(f"Action:   {parsed.get('action', 'N/A')}")
            print(f"Context:  {parsed.get('context', 'N/A')}")
            print(f"Country:  {parsed.get('country', 'N/A')}")
            print(f"State:    {parsed.get('state', 'N/A')}")
            print(f"City:     {parsed.get('city', 'N/A')}")
            print()
            print("Summary:")
            print(parsed.get('summary', 'N/A'))
        elif isinstance(result, list) and len(result) == 0:
            print("⚠️  Empty result (no scam detected or parsing failed)")
        else:
            print(f"⚠️  Unexpected result format: {type(result)}")
            
    except Exception as e:
        print("=" * 80)
        print("❌ Parsing Error")
        print("=" * 80)
        print(f"Error type: {type(e).__name__}")
        print(f"Error message: {str(e)}")
        import traceback
        print()
        print("Traceback:")
        traceback.print_exc()

if __name__ == "__main__":
    import sys
    
    if len(sys.argv) < 2:
        print("Usage: python test_single_parsing.py <reddit_id>")
        print()
        print("Example:")
        print("  python test_single_parsing.py abc123xyz")
        print()
        print("To find a reddit_id:")
        print("  mongo")
        print("  use travel_scam")
        print('  db.raw_reddit.findOne({"classification.is_travel_scam": true}, {reddit_id: 1})')
        sys.exit(1)
    
    reddit_id = sys.argv[1]
    test_single_parsing(reddit_id)


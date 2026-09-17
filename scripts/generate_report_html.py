import json
import os

def generate():
    report_path = os.path.join(os.path.dirname(__file__), 'docs', 'PROJECT_REPORT.md')
    output_path = os.path.join(os.path.dirname(__file__), 'docs', 'PROJECT_REPORT.html')

    with open(report_path, 'r', encoding='utf-8') as f:
        md_content = f.read()

    escaped_json = json.dumps(md_content)

    html = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>FinTrack CLI - Academic Project Report</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/5.5.1/github-markdown.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/github.min.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/marked/12.0.1/marked.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>
    <style>
        body {{
            background-color: #f6f8fa;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif;
            margin: 0;
            padding: 0;
            color: #24292f;
        }}
        .header-bar {{
            background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
            color: #ffffff;
            padding: 20px 36px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
            position: sticky;
            top: 0;
            z-index: 100;
        }}
        .header-bar h1 {{
            margin: 0;
            font-size: 20px;
            font-weight: 600;
            letter-spacing: -0.025em;
        }}
        .header-bar p {{
            margin: 4px 0 0 0;
            font-size: 13px;
            color: #94a3b8;
        }}
        .btn-group {{
            display: flex;
            gap: 12px;
        }}
        .btn {{
            background-color: #2563eb;
            color: #ffffff;
            border: none;
            padding: 9px 18px;
            font-size: 13px;
            font-weight: 600;
            border-radius: 6px;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            transition: all 0.15s ease;
        }}
        .btn:hover {{
            background-color: #1d4ed8;
            transform: translateY(-1px);
        }}
        .btn-secondary {{
            background-color: #334155;
        }}
        .btn-secondary:hover {{
            background-color: #475569;
        }}
        .container {{
            max-width: 960px;
            margin: 36px auto 60px auto;
            background: #ffffff;
            padding: 56px 64px;
            border-radius: 10px;
            box-shadow: 0 1px 3px 0 rgba(0,0,0,0.1), 0 1px 2px 0 rgba(0,0,0,0.06);
            border: 1px solid #e2e8f0;
        }}
        .markdown-body {{
            box-sizing: border-box;
            min-width: 200px;
            max-width: 980px;
            margin: 0 auto;
            font-size: 15px;
            line-height: 1.7;
        }}
        .markdown-body table {{
            display: table;
            width: 100%;
            border-collapse: collapse;
            margin: 16px 0;
        }}
        .markdown-body table th {{
            background-color: #f1f5f9;
        }}
        .markdown-body pre {{
            background-color: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 6px;
            padding: 16px;
        }}
        @media print {{
            .header-bar, .btn-group {{
                display: none !important;
            }}
            body {{
                background-color: #ffffff;
            }}
            .container {{
                max-width: 100%;
                margin: 0;
                padding: 10px 20px;
                box-shadow: none;
                border: none;
            }}
            @page {{
                margin: 1.5cm;
                size: A4;
            }}
        }}
    </style>
</head>
<body>
    <div class="header-bar">
        <div>
            <h1>FinTrack CLI — Comprehensive Academic Project Report</h1>
            <p>Object-Oriented Programming & Core Java | VIT Bhopal University | Ankit (24BCY10200)</p>
        </div>
        <div class="btn-group">
            <button class="btn" onclick="window.print()">🖨️ Print / Save as PDF</button>
            <a class="btn btn-secondary" href="https://github.com/ankit24bcy10200-creator/fintrack-cli" target="_blank">📂 GitHub Repository</a>
        </div>
    </div>
    <div class="container">
        <article id="content" class="markdown-body"></article>
    </div>
    <script>
        const mdText = {escaped_json};
        marked.setOptions({{
            gfm: true,
            breaks: false
        }});
        document.getElementById('content').innerHTML = marked.parse(mdText);
        hljs.highlightAll();
    </script>
</body>
</html>"""

    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(html)
    print(f"Generated {output_path} ({len(html)} bytes)")

if __name__ == '__main__':
    generate()

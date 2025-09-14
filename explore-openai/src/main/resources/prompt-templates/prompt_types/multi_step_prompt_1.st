对由三个反引号分隔的文本执行以下操作：

    1. 用一句话概括文本。
    2. 将摘要翻译成法语。
    3. 列出文本中提到的所有关键人物。
    4. 输出一个 JSON 对象，其结构如下：
    {{
        "summary": "<your summary>",
        "translation": "<translated summary>",
        "people": ["<person1>", "<person2>", ...]
    }}

    ```{input}```
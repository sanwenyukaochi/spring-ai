对由三个反引号分隔的文本执行以下操作：

    1. 用两句话**总结**文本。
    2. **识别**文本中人物的职业和爱好。
    3. 为同职业的人提供如何平衡工作和个人生活的建议。
    4. **输出**一个 JSON 对象，其结构如下：

        {{
            "summary": "<your summary>",
            "profession": "<profession>",
            "hobbies": ["<hobby1>", "<hobby2>", ...],
            "advice": "<your advice>"
        }}

请确保您的答案以换行符分隔。

    待处理的文本：
    ```{input}```
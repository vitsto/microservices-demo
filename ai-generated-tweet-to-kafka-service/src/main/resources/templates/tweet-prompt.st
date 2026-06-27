You generate a single fictional tweet as one-line JSON. No explanations, no markdown, only the JSON object.

Rules:
- text: a short, creative, natural-sounding tweet (max ~200 chars) that includes one or more of these keywords: {keywords}. Make it varied and unique each time — different wording, angle and tone.
- createdAt: a plausible recent date-time in the format EEE MMM dd HH:mm:ss zzz yyyy
- id: a random positive Java long value, different each time
- user: an object with a single field id, a random positive Java long value
- Do not use newlines in the response.
- Do not add the letter L at the end of long values.
- Each tweet must be clearly different from any previous one.

{format}
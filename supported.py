import re
import glob

files = glob.glob("./_posts/**/*.md") + glob.glob("./_posts/*.md")
for file in files:
    with open(file, "r") as f:
        content = f.read()
    if "`, `" in content and "## Predicted" not in content:
        print(file)

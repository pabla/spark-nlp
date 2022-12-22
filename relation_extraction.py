import json

f = open('/Users/pavel/Downloads/models.json', 'r')
data = json.load(f)
count = 0
for row in data:
    if not (row.get('task') and 'Relation Extraction' in row.get('task')):
        continue
    # if row.get('predicted_entities'):
    #     continue
    print('#', row['url'])
    print(row['download_link'])
    print('')
    print('---')
    print('')
    # print(row['url'])
    # print(row['download_link'])
    # print("---")
    count+=1

print(count)
f.close()
import xml.etree.ElementTree as ET
import re

file_path = 'app/src/main/res/layout/activity_mapa.xml'
reference_width = 328.0  # 360dp - 32dp margins

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Regex to find ImageViews with id starting with img
# We capture the whole tag content to modify it
pattern = re.compile(r'(<ImageView\s+[^>]*android:id="@/id/img[^>]*>)', re.DOTALL)

def replace_dimensions(match):
    tag_content = match.group(1)
    
    # Extract width
    width_match = re.search(r'android:layout_width="(\d+)dp"', tag_content)
    if not width_match:
        return tag_content # Skip if no dp width found
        
    width_dp = int(width_match.group(1))
    percent = width_dp / reference_width
    
    # Replace width with 0dp
    new_content = re.sub(r'android:layout_width="\d+dp"', 'android:layout_width="0dp"', tag_content)
    
    # Replace height with 0dp
    new_content = re.sub(r'android:layout_height="\d+dp"', 'android:layout_height="0dp"', new_content)
    
    # Update or Add constraintWidth_percent
    percent_str = f'{percent:.4f}'
    if 'app:layout_constraintWidth_percent' in new_content:
        new_content = re.sub(r'app:layout_constraintWidth_percent="[^"]*"', f'app:layout_constraintWidth_percent="{percent_str}"', new_content)
    else:
        # Insert it before the closing />
        new_content = new_content.rsplit('/>', 1)[0] + f'\n            app:layout_constraintWidth_percent="{percent_str}" />'
        
    return new_content

new_content = pattern.sub(replace_dimensions, content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(new_content)

print("Layout updated successfully.")

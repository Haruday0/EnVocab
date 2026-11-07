"""
App Icon Generator for EnVocab
Generates adaptive icon drawables (foreground, monochrome, background) using fontTools
from the bundled Google Sans variable font.
"""

import os
from fontTools.ttLib import TTFont
from fontTools.varLib.instancer import instantiateVariableFont
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen
from fontTools.pens.boundsPen import BoundsPen

def generate_icons(
    font_path: str = 'app/src/main/res/font/google_sans.ttf',
    res_drawable_dir: str = 'app/src/main/res/drawable',
    lines: tuple = ("En", "Vocab"),
    weight: float = 700.0,
    text_color: str = "#1C1B1F",
    bg_color: str = "#FFFFFF",
    target_width_dp: float = 58.0,
    target_left_dp: float = 25.0,
    target_center_y_dp: float = 53.5,
    line_spacing: float = 1600.0,
    optical_e_offset: float = -60.0
):
    font = TTFont(font_path)
    inst_font = instantiateVariableFont(font, {'wght': weight, 'opsz': 72.0})
    glyph_set = inst_font.getGlyphSet()
    cmap = inst_font.getBestCmap()
    hmtx = inst_font['hmtx']

    line_data = []
    for line_idx, line in enumerate(lines):
        x_cursor = 0.0
        line_glyphs = []
        for char in line:
            gname = cmap[ord(char)]
            adv_w, lsb = hmtx[gname]
            bpen = BoundsPen(glyph_set)
            glyph_set[gname].draw(bpen)
            bounds = bpen.bounds
            line_glyphs.append({
                'char': char,
                'gname': gname,
                'x': x_cursor,
                'adv_w': adv_w,
                'bounds': bounds
            })
            x_cursor += adv_w
        line_data.append({
            'text': line,
            'glyphs': line_glyphs,
            'width': x_cursor
        })

    line_x_offsets = [optical_e_offset, 0.0]

    all_bounds_xMin = []
    all_bounds_yMin = []
    all_bounds_xMax = []
    all_bounds_yMax = []

    for line_idx, linfo in enumerate(line_data):
        y_baseline = line_spacing if line_idx == 0 else 0.0
        x_off = line_x_offsets[line_idx]
        for g in linfo['glyphs']:
            gb = g['bounds']
            all_bounds_xMin.append(x_off + g['x'] + gb[0])
            all_bounds_xMax.append(x_off + g['x'] + gb[2])
            all_bounds_yMin.append(y_baseline + gb[1])
            all_bounds_yMax.append(y_baseline + gb[3])

    min_x = min(all_bounds_xMin)
    max_x = max(all_bounds_xMax)
    min_y = min(all_bounds_yMin)
    max_y = max(all_bounds_yMax)

    text_w = max_x - min_x
    text_h = max_y - min_y

    scale = target_width_dp / text_w
    render_w = text_w * scale
    render_h = text_h * scale

    tx = target_left_dp - scale * min_x
    ty = target_center_y_dp + scale * (min_y + max_y) / 2.0

    svg_paths = []
    for line_idx, linfo in enumerate(line_data):
        y_baseline = line_spacing if line_idx == 0 else 0.0
        x_off = line_x_offsets[line_idx]
        for g in linfo['glyphs']:
            gx = x_off + g['x']
            gy = y_baseline
            matrix = (scale, 0, 0, -scale, scale * gx + tx, -scale * gy + ty)

            svg_pen = SVGPathPen(glyph_set)
            t_pen = TransformPen(svg_pen, matrix)
            glyph_set[g['gname']].draw(t_pen)
            d = svg_pen.getCommands()
            if d:
                svg_paths.append(d)

    full_path_data = " ".join(svg_paths)

    # 1. Foreground VectorDrawable
    vd_foreground = f"""<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="{text_color}"
        android:pathData="{full_path_data}" />
</vector>
"""

    # 2. Monochrome VectorDrawable (Android 13+ Themed Icons)
    vd_monochrome = f"""<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#000000"
        android:pathData="{full_path_data}" />
</vector>
"""

    # 3. Background VectorDrawable (White background)
    vd_background = f"""<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="{bg_color}"
        android:pathData="M0,0h108v108h-108z" />
</vector>
"""

    os.makedirs(res_drawable_dir, exist_ok=True)
    with open(os.path.join(res_drawable_dir, 'ic_launcher_foreground.xml'), 'w', encoding='utf-8') as f:
        f.write(vd_foreground)
    with open(os.path.join(res_drawable_dir, 'ic_launcher_monochrome.xml'), 'w', encoding='utf-8') as f:
        f.write(vd_monochrome)
    with open(os.path.join(res_drawable_dir, 'ic_launcher_background.xml'), 'w', encoding='utf-8') as f:
        f.write(vd_background)

    print(f"Generated VectorDrawables in {res_drawable_dir} (text: {lines}, size: {render_w:.1f}x{render_h:.1f}dp)")

if __name__ == '__main__':
    generate_icons()

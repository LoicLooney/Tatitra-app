"""Régénère les icônes : web coins arrondis, mobile moins zoomée."""
from PIL import Image, ImageDraw
from pathlib import Path

ROOT = Path(r"d:\Master\Développement Mobile\Tatitra-app")
SRC = ROOT / "docs/branding/ic_launcher.png"
BRAND_BLUE = (0, 92, 219, 255)

FG_SIZES = {
    "mipmap-mdpi": 108,
    "mipmap-hdpi": 162,
    "mipmap-xhdpi": 216,
    "mipmap-xxhdpi": 324,
    "mipmap-xxxhdpi": 432,
}
LEGACY_SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}


def blanchir_coins(src: Image.Image) -> Image.Image:
    """Rend transparents les coins blancs autour du squircle bleu."""
    src = src.convert("RGBA")
    pixels = src.load()
    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = pixels[x, y]
            if r > 235 and g > 235 and b > 235:
                pixels[x, y] = (0, 0, 0, 0)
    return src


def placer(src: Image.Image, size: int, ratio: float, fond) -> Image.Image:
    """Place l'icône branding réduite au centre (ratio < 1 = moins zoomée)."""
    canvas = Image.new("RGBA", (size, size), fond)
    side = max(1, int(size * ratio))
    resized = src.resize((side, side), Image.Resampling.LANCZOS)
    ox = (size - side) // 2
    oy = (size - side) // 2
    canvas.paste(resized, (ox, oy), resized)
    return canvas


def symbole_sur_transparent(src: Image.Image, size: int, ratio: float) -> Image.Image:
    """Foreground adaptive : icône réduite, fond transparent (le bleu adaptive apparaît)."""
    return placer(src, size, ratio, (0, 0, 0, 0))


def legacy_carre(src: Image.Image, size: int, ratio: float) -> Image.Image:
    return placer(src, size, ratio, BRAND_BLUE)


def legacy_rond(src: Image.Image, size: int, ratio: float) -> Image.Image:
    base = legacy_carre(src, size, ratio)
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).ellipse((0, 0, size - 1, size - 1), fill=255)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.paste(base, (0, 0), mask)
    return out


def favicon_arrondi(src: Image.Image, size: int, ratio: float, radius_ratio: float = 0.28) -> Image.Image:
    """Favicon web : même branding, plus petit, masqué en squircle."""
    base = placer(src, size, ratio, BRAND_BLUE)
    radius = int(size * radius_ratio)
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, size - 1, size - 1), radius=radius, fill=255)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.paste(base, (0, 0), mask)
    return out


def main() -> None:
    src = blanchir_coins(Image.open(SRC))
    res = ROOT / "app/src/main/res"

    # Mobile : ~58 % du canvas → marge bleue plus large (moins « zoomé »)
    ratio_mobile = 0.58
    for folder, size in FG_SIZES.items():
        symbole_sur_transparent(src, size, ratio_mobile).save(
            res / folder / "ic_launcher_foreground.png", "PNG"
        )
        print("FG", size)

    for folder, size in LEGACY_SIZES.items():
        legacy_carre(src, size, ratio_mobile).save(res / folder / "ic_launcher.png", "PNG")
        legacy_rond(src, size, ratio_mobile).save(res / folder / "ic_launcher_round.png", "PNG")
        print("LEGACY", size)

    fav = ROOT / "admin-web/public"
    # Web : coins bien arrondis
    favicon_arrondi(src, 192, 0.92, 0.30).save(fav / "favicon.png", "PNG")
    favicon_arrondi(src, 32, 0.92, 0.30).save(fav / "favicon-32.png", "PNG")
    favicon_arrondi(src, 180, 0.92, 0.30).save(fav / "apple-touch-icon.png", "PNG")
    print("WEB ok")


if __name__ == "__main__":
    main()

const sharp = require("sharp");
const parsedarg = JSON.parse(process.argv[2]);

function resizeImg(src, srcWithoutExtension, widths, format) {
  widths.map(width => {
    sharp(src)
      .resize(width, null)
      .toFile(`public/${srcWithoutExtension}-${width}.${format}`)
      .then(info => {})
      .catch(err => {});
  });
}

(() => {
  let src = parsedarg.src,
    srcWithoutExtension = parsedarg.srcWithoutExtension,
    widths = parsedarg.widths,
    formats = parsedarg.formats;

  formats.webp === true
    ? resizeImg(src, srcWithoutExtension, widths, "webp")
    : null;

  formats.jpg === true
    ? resizeImg(src, srcWithoutExtension, widths, "jpg")
    : null;

  formats.png === true
    ? resizeImg(src, srcWithoutExtension, widths, "png")
    : null;
})();

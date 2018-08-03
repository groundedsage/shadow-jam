const critical = require("critical");
const fs = require("fs");
var files = fs.readdirSync("./public");

const HTMLfiles = files.filter(
  s =>
    s == "css" || s == "js" || s == "images" || s == "subfont" ? false : true
);

HTMLfiles.map(i =>
  critical.generate({
    base: "public/",
    inline: true,
    src: i,
    dest: i,
    ignore: ["@font-face", /url\(/],
    width: 1300,
    height: 900
  })
);

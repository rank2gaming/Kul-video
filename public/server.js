const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');
const crypto = require('crypto');

const PORT = 3000;
const PUBLIC_DIR = __dirname;
const APK_PATH = path.join(PUBLIC_DIR, 'CineFlow-AI-debug.apk');

let cachedMtimeMs = 0;
let cachedSha256 = '';

function getApkMeta() {
  const stat = fs.statSync(APK_PATH);
  if (stat.mtimeMs !== cachedMtimeMs || !cachedSha256) {
    const buf = fs.readFileSync(APK_PATH);
    cachedSha256 = crypto.createHash('sha256').update(buf).digest('hex');
    cachedMtimeMs = stat.mtimeMs;
  }
  return {
    filename: 'CineFlow-AI-debug.apk',
    size: stat.size,
    sha256: cachedSha256
  };
}

const server = http.createServer((req, res) => {
  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname || '/';

  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, HEAD, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Range, Content-Type');
  res.setHeader('Access-Control-Expose-Headers', 'Content-Range, Content-Length, Accept-Ranges');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  // 1. Metadata endpoint for landing page & chunked downloader
  if (pathname === '/api/apk-info') {
    const meta = getApkMeta();
    res.writeHead(200, {
      'Content-Type': 'application/json',
      'Cache-Control': 'no-cache'
    });
    res.end(JSON.stringify(meta));
    return;
  }

  // 2. Explicit 4MB chunk endpoint (bypasses Cloud Run 32MB single-response limit)
  if (pathname === '/api/apk-chunk') {
    const stat = fs.statSync(APK_PATH);
    const total = stat.size;
    const start = Math.max(0, parseInt(parsedUrl.query.start || '0', 10));
    const end = Math.min(total - 1, parseInt(parsedUrl.query.end || String(total - 1), 10));

    if (isNaN(start) || isNaN(end) || start > end || start >= total) {
      res.writeHead(416, { 'Content-Range': `bytes */${total}` });
      res.end();
      return;
    }

    const chunkLength = end - start + 1;
    res.writeHead(200, {
      'Content-Type': 'application/octet-stream',
      'Content-Length': chunkLength,
      'Cache-Control': 'no-store'
    });
    fs.createReadStream(APK_PATH, { start, end }).pipe(res);
    return;
  }

  // 3. Direct APK request with HTTP Range support + landing page fallback for HTML navigation
  if (pathname === '/CineFlow-AI-debug.apk' || pathname === '/app-debug.apk') {
    const stat = fs.statSync(APK_PATH);
    const total = stat.size;
    const rangeHeader = req.headers.range;
    const acceptHeader = req.headers.accept || '';

    if (!rangeHeader && req.method === 'GET' && acceptHeader.includes('text/html') && parsedUrl.query.raw !== '1') {
      const html = fs.readFileSync(path.join(PUBLIC_DIR, 'index.html'), 'utf8');
      res.writeHead(200, {
        'Content-Type': 'text/html; charset=utf-8',
        'Cache-Control': 'no-cache'
      });
      res.end(html);
      return;
    }

    if (rangeHeader) {
      const parts = rangeHeader.replace(/bytes=/, '').split('-');
      const start = parseInt(parts[0], 10);
      const requestedEnd = parts[1] ? parseInt(parts[1], 10) : total - 1;
      const maxRangeEnd = Math.min(total - 1, start + 8 * 1024 * 1024 - 1);
      const end = Math.min(requestedEnd, maxRangeEnd);

      if (isNaN(start) || start >= total || start > end) {
        res.writeHead(416, { 'Content-Range': `bytes */${total}` });
        res.end();
        return;
      }

      const chunkSize = end - start + 1;
      res.writeHead(206, {
        'Content-Range': `bytes ${start}-${end}/${total}`,
        'Accept-Ranges': 'bytes',
        'Content-Length': chunkSize,
        'Content-Type': 'application/vnd.android.package-archive',
        'Content-Disposition': 'attachment; filename="CineFlow-AI-debug.apk"'
      });
      if (req.method === 'HEAD') {
        res.end();
        return;
      }
      fs.createReadStream(APK_PATH, { start, end }).pipe(res);
      return;
    }

    res.writeHead(200, {
      'Accept-Ranges': 'bytes',
      'Content-Length': total,
      'Content-Type': 'application/vnd.android.package-archive',
      'Content-Disposition': 'attachment; filename="CineFlow-AI-debug.apk"'
    });
    if (req.method === 'HEAD') {
      res.end();
      return;
    }
    fs.createReadStream(APK_PATH).pipe(res);
    return;
  }

  // 4. Landing page (index.html)
  const indexHtml = fs.readFileSync(path.join(PUBLIC_DIR, 'index.html'), 'utf8');
  res.writeHead(200, {
    'Content-Type': 'text/html; charset=utf-8',
    'Cache-Control': 'no-cache'
  });
  res.end(indexHtml);
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`CineFlow AI APK Server listening on port ${PORT}`);
});

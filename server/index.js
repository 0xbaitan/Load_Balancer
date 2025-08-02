const express = require('express');
const os = require('os');
const app = express();
const port = 3000;

app.get('/', (req, res) => {
  res.send(`Hello World! from ${os.hostname()}` );
});

app.listen(port, () => {
  console.log(`Server running on http://${os.hostname()}:${port}`);
});

const express = require('express');
const os = require('os');
const app = express();
const port = 3000;



app.get('/', (req, res) => {
  setTimeout(() => {
    console.log('Waiting for random time before responding...');
    res.send(`Hello World! from ${os.hostname()}` );
  }, Math.floor(Math.random() * 300)); // Simulate random delay up to 300 ms
 
});

app.get('/health', (req, res) => {
  const seed = Math.random();
  if (seed < 0.05) {
    console.error('Simulated failure for health check');
    return res.status(500).send('Service Unavailable');
  }
  console.log('Health check received');
  res.status(200).send('OK');
});

app.listen(port, () => {
  console.log(`Server running on http://${os.hostname()}:${port}`);
});

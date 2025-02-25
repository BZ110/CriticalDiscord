const mysql = require('mysql2');

function createDBPool(connectionString) {
  // Create a connection pool using the connection string (JDBC-style strings work if properly formatted)
  if(connectionString.includes('jdbc:')) {
    // First 5 characters are "jdbc:", so we remove them
    connectionString = connectionString.substring(5);
    }

    
  const pool = mysql.createPool(connectionString);

  // Test the connection once at startup
  pool.getConnection((err, connection) => {
    if (err) {
      console.error('Error connecting to MySQL:', err);
    } else {
      console.log('Connected to MySQL');
      connection.release();
    }
  });

  // Set up a periodic ping to refresh the connection if it times out
  setInterval(() => {
    pool.query('SELECT 1', (err) => {
      if (err) {
        console.error('MySQL connection lost, attempting to reconnect:', err);
      }
    });
  }, 60000); // every 60 seconds

  // Return a promise-based pool for easier async/await usage
  return pool.promise();
}

module.exports = createDBPool;

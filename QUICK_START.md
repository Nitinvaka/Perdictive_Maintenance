# Quick Start Guide

Get your Predictive Maintenance system running in 5 minutes!

## Prerequisites

- Java 17+ (for backend)
- Node.js 16+ (for frontend)
- Maven (comes with the project)

## Step 1: Start the Backend

```bash
# Navigate to backend folder
cd backend/PredictiveMaintenanceLite

# Run the Spring Boot application
./mvnw spring-boot:run

# On Windows use:
# mvnw.cmd spring-boot:run
```

The backend will start on **http://localhost:8080**

## Step 2: Start the Frontend

Open a new terminal window:

```bash
# Navigate to frontend folder
cd frontend

# Install dependencies (first time only)
npm install

# Start the development server
npm run dev
```

The frontend will start on **http://localhost:5173**

## Step 3: Open in Browser

Visit: **http://localhost:5173**

Default credentials:
- **Username**: admin@pml.com
- **Password**: admin123

## What to Try First

### 1. Create Your First Asset
1. Go to **Assets** page
2. Click **"New Asset"** button
3. Fill in:
   - Name: "Pump-01"
   - Location: "Plant A - Floor 1"
   - Add a sensor (e.g., "Vibration Sensor A")
4. Click **"Create Asset"**

### 2. Try the Search
1. On **Assets** page
2. Type in the search box: "Pump"
3. See results filter in real-time

### 3. Test the Simulator
1. Go to **Simulator** page
2. Select your sensor from dropdown
3. Enter some values:
   - RMS: 7.5 (above threshold!)
   - Temperature: 98
4. Click **"Publish Payload"**
5. A ticket will be created automatically

### 4. Check Inactive Asset Protection
1. On **Assets** page, click on your asset's sensor count
2. Try deactivating the asset
3. Notice you cannot add new sensors (protected!)

## Troubleshooting

### Backend won't start?
- Check if Java 17+ is installed: `java -version`
- Make sure port 8080 is available
- Check console for error messages

### Frontend won't start?
- Check if Node.js is installed: `node -v`
- Try deleting `node_modules` and running `npm install` again
- Make sure port 5173 is available

### Can't login?
- Make sure backend is running first
- Check backend console for errors
- Default credentials: admin@pml.com / admin123

## Need Help?

Check these files:
- **README.md** - Full documentation
- **CHANGES.md** - List of all changes made
- Comments in code files - Each page has explanation at the top

## Quick Commands

### Backend
```bash
# Start backend
cd backend/PredictiveMaintenanceLite && ./mvnw spring-boot:run

# Stop backend
Ctrl+C
```

### Frontend
```bash
# Start frontend
cd frontend && npm run dev

# Build for production
npm run build

# Stop frontend
Ctrl+C
```

## What's Different in This Version?

✅ Asset search by name
✅ Cannot add sensors to inactive assets
✅ Cannot create thresholds manually (auto-created with assets)
✅ "New Asset" button hidden in violations view
✅ Friendly comments throughout code
✅ Simplified React patterns for beginners

---

**You're all set! Enjoy your Predictive Maintenance system! 🚀**

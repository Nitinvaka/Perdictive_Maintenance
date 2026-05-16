# Predictive Maintenance Lite

This project has been cleaned and simplified according to your requirements. The backend remains unchanged, and the frontend has been updated with better user experience and simpler React code.

## 📁 Project Structure

```
pml-project/
├── backend/                    # Spring Boot backend
│   └── PredictiveMaintenanceLite/
└── frontend/                   # React frontend 
    └── src/
        ├── api/                # API client functions
        ├── components/         # Reusable UI components
        ├── pages/              # Main application pages
        └── utils/              # Helper functions
```

## 🚀 How to Run

### Backend (Port 8080)
```bash
cd backend/PredictiveMaintenanceLite
./mvnw spring-boot:run
```

### Frontend (Port 5173)
```bash
cd frontend
npm install
npm run dev
```

## 📖 Page Guide

### **Assets Page** (`src/pages/Assets.jsx`)
- View all your industrial assets (pumps, motors, etc.)
- **Search** by asset name using the search box
- Click on sensor count to manage sensors for each asset
- **Cannot add sensors to inactive assets** - you'll see a red warning
- Create new assets with sensors and thresholds in one go
- Filter to show only assets with violations (threshold breaches in last 24 hours)
- ⚠️ **"Add Asset" button is hidden when viewing violations**

### **Sensors Page** (`src/pages/Sensors.jsx`)
- View all sensors across all assets
- See sensor type (Vibration, Temperature, or Combined)
- Activate or deactivate sensors
- Filter to show only active sensors

### **Simulator Page** (`src/pages/Simulator.jsx`)
- Test sensor readings by simulating IoT device payloads
- **Only shows active sensors from active assets** (inactive ones are blocked)
- See if your values will breach thresholds before submitting
- Automatically creates maintenance tickets when thresholds are exceeded

### **Thresholds Page** (`src/pages/Thresholds.jsx`)
- View safety thresholds for each asset
- Edit existing thresholds
- ⚠️ **Cannot create new thresholds here** - they're created automatically when you add an asset
- Thresholds define max RMS (vibration) and temperature limits

## 🔒 Security Features

### Inactive Asset Protection
When an asset is inactive:
- ❌ Cannot add new sensors to it
- ❌ Cannot select it in the simulator
- ✅ Shows clear warning messages
- ✅ Existing sensors can still be viewed (but not added)

This prevents accidentally monitoring or simulating readings for equipment that's offline or under maintenance.

## 🎨 User Interface

The UI remains the same beautiful design with:
- Clean, modern look
- Color-coded sensor types
- Status indicators (active/inactive)
- Responsive tables
- Helpful empty states
- Loading skeletons

## 📝 Code Style

All code now includes:
- Simple, beginner-friendly React patterns
- Clear variable names
- Friendly comments explaining what each section does
- No complex hooks or advanced patterns
- Straightforward logic that's easy to follow

## 🔧 API Changes

### Assets API (`src/api/assets.js`)
Updated `getAll()` to support search by name:
```javascript
// Search for assets by name
assetsApi.getAll('Pump')  // Returns only assets with "Pump" in the name
assetsApi.getAll('')      // Returns all assets
```

## 🎯 Key Behavior Changes

1. **Asset Creation**: When you create a new asset, the threshold is automatically created - no separate step needed

2. **Violations View**: When viewing assets with violations, you cannot add new assets (the button is hidden)

3. **Search**: Type in the search box on the Assets page to filter by asset name - results update in real-time

4. **Inactive Assets**: Clear visual indicators and blocking when trying to add sensors to inactive assets

## 💡 Tips for Beginners

- Start by reading the comments at the top of each page file (Assets.jsx, Sensors.jsx, etc.)
- The `useState` hooks are clearly commented to show what each state variable does
- API calls are separated into `src/api/` files for easy understanding
- UI components are in `src/components/ui/` and can be reused


## 🔄 Next Steps

1. Run the backend and frontend
2. Create some assets with sensors
3. Try the simulator with different values
4. See how tickets are created automatically when thresholds are breached
5. Search for assets by name
6. Try to add a sensor to an inactive asset (you'll see it's blocked)

---

**Enjoy your cleaned and simplified Predictive Maintenance system! 🎉**

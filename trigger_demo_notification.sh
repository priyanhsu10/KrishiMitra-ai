#!/bin/bash

# KrishiMitra Demo Notification Trigger Script
# This script sends a high-priority push notification to a specific farmer.

# Colors for better visibility
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}==============================================================${NC}"
echo -e "${GREEN}       🌾 KrishiMitra - Push Notification Demo Trigger 🌾      ${NC}"
echo -e "${BLUE}==============================================================${NC}"

# 1. Get Farmer ID
echo -e "\n${YELLOW}Step 1: Get the Farmer ID${NC}"
echo "Check your Android Studio Logcat for 'FCM token updated successfully' to find the ID,"
echo "or check your database 'farmers' table."
read -p "Enter Farmer UUID: " FARMER_ID

if [ -z "$FARMER_ID" ]; then
    echo -e "${RED}Error: Farmer ID is required.${NC}"
    exit 1
fi

# 2. Choose Alert Type
echo -e "\n${YELLOW}Step 2: Choose Alert Type${NC}"
echo "1) Weather (Heavy Rain)"
echo "2) Disease (Soybean Rust Detected)"
echo "3) Mandi (Price Spike - Sell Now)"
read -p "Choose (1-3): " CHOICE

case $CHOICE in
    1)
        TYPE="weather"
        MSG_EN="Heavy rain (45mm) expected in 2 hours. Move harvested crop to a safe place."
        MSG_MR="येत्या २ तासात अतिवृष्टी (४५ मिमी) होण्याची शक्यता आहे. काढणी केलेले पीक सुरक्षित ठिकाणी हलवा."
        ;;
    2)
        TYPE="disease"
        MSG_EN="High risk of Soybean Rust detected in your area. Apply Fungicide today."
        MSG_MR="तुमच्या भागात सोयाबीन तांबेरा रोगाचा मोठा धोका आढळला आहे. आजच बुरशीनाशकाची फवारणी करा."
        ;;
    3)
        TYPE="market"
        MSG_EN="Soybean prices in Pune Mandi jumped to ₹5,200! Best time to sell."
        MSG_MR="पुणे मंडीत सोयाबीनचे भाव ₹५,२०० पर्यंत वाढले आहेत! विक्रीसाठी ही सर्वोत्तम वेळ आहे."
        ;;
    *)
        TYPE="weather"
        MSG_EN="Emergency Alert from KrishiMitra."
        MSG_MR="कृषिमित्र कडून आणीबाणीची सूचना."
        ;;
esac

# 3. Send the request
echo -e "\n${YELLOW}Step 3: Triggering Notification via Backend...${NC}"

# Note: Using localhost:8080. Update if your backend is running elsewhere.
RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/notify \
  -H "Content-Type: application/json" \
  -d "{
    \"farmerId\": \"$FARMER_ID\",
    \"alertType\": \"$TYPE\",
    \"messageEn\": \"$MSG_EN\",
    \"messageMr\": \"$MSG_MR\",
    \"priority\": \"high\"
  }")

# 4. Check Result
if [[ $RESPONSE == *"fcmSent\":true"* ]]; then
    echo -e "${GREEN}✅ SUCCESS! Notification sent to FCM.${NC}"
    echo -e "${YELLOW}Your phone should ring/vibrate now! 🔔${NC}"
else
    echo -e "${RED}❌ FAILED.${NC}"
    echo "Response: $RESPONSE"
    echo -e "\n${YELLOW}Troubleshooting:${NC}"
    echo "1. Ensure Backend is running on port 8080."
    echo "2. Ensure the Farmer ID exists in the database."
    echo "3. Ensure the app is logged in on the phone (to register FCM token)."
    echo "4. Check backend logs for 'Firebase credentials file not found'."
fi

echo -e "${BLUE}==============================================================${NC}"

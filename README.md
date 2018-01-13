## Inventory AR

**Inventory AR** is an application that uses Google Vision to provide an AR experience to a store’s inventory and merchandising activities.   Try it out for yourself in your environment – _it’s free_ – and let us know how we can make it better.

#### Get started:


-	Download and install APK (see binaries folder for the latest version);
-	Create a **products.json** file from your own product/inventory data and upload to the device;
-	Make sure your device is connected to the internet (for first time install/open only) and Open the Application.  

_Note: Internet connection is only required on first startup to let the application download Google libraries required for barcode and text recognition.  Once the app is operational all decoding can be performed offline._

#### The products.json file

To make your own products file, create a JSON file named **products.json** with the following data/field structure:

```javascript
[
  {
    "barcode": "512687900536",
    "dpci": 42560,
    "description": "6pk 12oz Three Taverns Night On Ponce ",
    "label": "Night on Ponce IPA ",
    "price": 9.25,
    "units": 6,
    "unitType": "pc",
    "unitsSoldLastWeek": 12,
    "avgWeeklySales": "13",
    "salesTrend": "-8%",
    "onHand": 14,
    "reorderThreshold": 10,
    "unitTrendData": [
      "24",
      "23",
      "24",
       ...
      "31",
      "14"
    ],
    "distributor": "National Distributing Company",
    "phoneNumber": "404-696-9440",
    "salesTrendData": [
      "24",
      "23",
      "24",
       ...
      "31",
      "21"
    ],
    "onHandTrendData": [
      "14",
      "13",
      "10",
       ...
      "14",
      "15"
    ]
  },
  {
      // next product
  },
  ...
]
```

The completed file should be uploaded to: 

```
/storage/0/emulated/Android/data/com.bluefletch.visioninventory/files/InventoryAR/product.json
```

#### Questions/Issues?

Feel free to ask questions to **alan.lampa@bluefletch.com**


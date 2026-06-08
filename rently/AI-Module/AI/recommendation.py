import sys
import json

def trapezoidal(r, x):
    a = 0
    b = r
    c = r
    d = r * 1.2

    if x < a or x > d:
        return 0.0
    elif a <= x <= b:
        return 1.0
    elif b < x <= d:
        return (d - x) / (d - c)
    else:
        return 0.0

def triangular(a, b, c, x):
    if a <= x <= b:
        return (x - a) / (b - a)
    elif b <= x <= c:
        return (c - x) / (c - b)
    else:
        return 0.0

cities = {
    'Warsaw': {'latitude': 52.2297, 'longitude': 21.0122, 'radius': 12.83},
    'Krakow': {'latitude': 50.0647, 'longitude': 19.9450, 'radius': 10.20},
    'Lodz': {'latitude': 51.7592, 'longitude': 19.4550, 'radius': 9.66},
    'Wroclaw': {'latitude': 51.1079, 'longitude': 17.0385, 'radius': 9.66},
    'Poznan': {'latitude': 52.4064, 'longitude': 16.9252, 'radius': 9.14},
    'Gdansk': {'latitude': 54.3520, 'longitude': 18.6466, 'radius': 9.14},
    'Katowice': {'latitude': 50.2649, 'longitude': 19.0238, 'radius': 7.23},
    'Plock': {'latitude': 52.5468, 'longitude': 19.7064, 'radius': 5.29},
    'Torun': {'latitude': 53.0138, 'longitude': 18.5984, 'radius': 6.05},
}

results = []


def main():
    input_json = sys.stdin.read()
    data = json.loads(input_json)

    recommendation = data["recommendationRequest"]
    apartments = data["apartments"]
    city = cities[recommendation['location']]

    for apartment in apartments:
        temp = []
        lat = abs(apartment['latitude'] - city['latitude']) * 111
        lon = abs(apartment['longitude'] - city['longitude']) * 111
        distance = (lat ** 2 + lon ** 2) ** 0.5
        temp.append(trapezoidal(city['radius'], distance))
        temp.append(trapezoidal(apartment['price'], recommendation['price']))
        temp.append(triangular(int(recommendation['rooms']) - 1, int(recommendation['rooms']), int(recommendation['rooms']) + 1, apartment['rooms']))
        score = min(temp)
        results.append((apartment['id'], score))

    result = {
        "status": "ok",
        "recommendations": [
            {
                "id": estate_id,
                "score": round(score, 4),
            }
            for estate_id, score in sorted(results, key=lambda x: x[1], reverse=True)
            if score > 0.0
        ]
    }

    print(json.dumps(result))

if __name__ == "__main__":
    main()

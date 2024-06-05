from freeGPT import AsyncClient
from asyncio import run
import asyncio
import sys

async def main():
    try:
        with open("cache.txt", "r", encoding="utf-8", errors='ignore') as file:
            prompt = file.read()
            try:
                resp = await AsyncClient.create_completion("gpt3", prompt)
                print(f"{resp}")
                with open("response.txt", "w", encoding='UTF-8') as response_file:
                    response_file.write(f"{resp}")
            except Exception as e:
                print(f"{e}")
    except FileNotFoundError:
        print("Файл не найден")

run(main())
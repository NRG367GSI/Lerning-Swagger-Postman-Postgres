import os
import datetime

def find_and_document_java_project(start_dir=".", output_dir=".", output_prefix="project_info"):
    """
    Сканирует каталоги вверх от start_dir в поисках корневого каталога Java-проекта
    (наличие папки 'src' или файла pom.xml/build.gradle).
    Сохраняет структуру и содержимое всех найденных .java файлов в Markdown-файл
    с именем: год_месяц_число_частьКорневогоПакета_времяИзменения.md

    Args:
        start_dir (str): Начальный каталог для поиска (по умолчанию: текущий каталог).
        output_dir (str): Каталог для сохранения Markdown-файла (по умолчанию: текущий каталог).
        output_prefix (str): Префикс для имени выходного файла (используется, если не удается определить часть корневого пакета).
    """

    root_project_dir = None
    current_dir = os.path.abspath(start_dir)

    print(f"Начинаю поиск корневого каталога с: {current_dir}")

    while current_dir:
        found_criteria = False
        if os.path.exists(os.path.join(current_dir, "src")):
            print(f"Найдена папка 'src' в: {current_dir}")
            found_criteria = True
        if os.path.exists(os.path.join(current_dir, "pom.xml")):
            print(f"Найден файл 'pom.xml' в: {current_dir}")
            found_criteria = True
        if os.path.exists(os.path.join(current_dir, "build.gradle")):
            print(f"Найден файл 'build.gradle' в: {current_dir}")
            found_criteria = True

        if found_criteria:
            root_project_dir = current_dir
            print(f"Корневой каталог проекта найден: {root_project_dir}")
            break

        parent_dir = os.path.dirname(current_dir)
        if parent_dir == current_dir:  # Достигнут корень файловой системы
            print("Достигнут корень файловой системы, корневой каталог проекта не найден.")
            break
        current_dir = parent_dir
        print(f"Перехожу в родительский каталог: {current_dir}")

    if not root_project_dir:
        print("Корневой каталог Java-проекта не найден.")
        return

    # Определение части корневого пакета
    root_package_part = None
    first_package = None
    found_java_file = False
    for root, _, files in os.walk(root_project_dir):
        for file in files:
            if file.endswith(".java"):
                found_java_file = True
                filepath = os.path.join(root, file)
                package_path_relative = os.path.relpath(root, root_project_dir).replace(os.sep, ".")
                package_str = None
                if package_path_relative.startswith("src.main.java."):
                    package_str = package_path_relative[len("src.main.java."):].replace(os.sep, ".")
                elif package_path_relative.startswith("src.java."):
                    package_str = package_path_relative[len("src.java."):].replace(os.sep, ".")
                elif package_path_relative.startswith("src."):
                    package_str = package_path_relative[len("src."):].replace(os.sep, ".")

                if package_str and not first_package:
                    first_package = package_str
                    break # Берем пакет первого попавшегося Java файла

        if first_package:
            break # Прерываем внешний цикл, как только нашли первый пакет

    if first_package:
        segments = first_package.split('.')
        if len(segments) >= 2:
            if segments[0] == 'ru' and len(segments) >= 3:
                root_package_part = ".".join(segments[1:3])
            elif len(segments) >= 2:
                root_package_part = ".".join(segments[:2])
            else:
                root_package_part = first_package
        else:
            root_package_part = first_package

    package_structure = {}
    for root, _, files in os.walk(root_project_dir):
        for file in files:
            if file.endswith(".java"):
                filepath = os.path.join(root, file)
                package_path_relative = os.path.relpath(root, root_project_dir).replace(os.sep, ".")
                if package_path_relative.startswith("src.main.java."):
                    package_path_relative = package_path_relative[len("src.main.java."):]
                elif package_path_relative.startswith("src.java."):
                    package_path_relative = package_path_relative[len("src.java."):]
                elif package_path_relative.startswith("src."):
                    package_path_relative = package_path_relative[len("src."):]

                if package_path_relative not in package_structure:
                    package_structure[package_path_relative] = []
                package_structure[package_path_relative].append((os.path.basename(filepath), filepath))

    timestamp = datetime.datetime.now().strftime("%H%M%S")
    date_str = datetime.datetime.now().strftime("%Y_%m_%d")
    package_part_for_filename = root_package_part if root_package_part else output_prefix
    output_filename = os.path.join(output_dir, f"{date_str}_{package_part_for_filename}_{timestamp}.md")

    print(f"Попытка записи в файл: {output_filename}")

    try:
        with open(output_filename, "w", encoding="utf-8") as outfile:
            outfile.write("# Информация о Java проекте\n\n")
            sorted_packages = sorted(package_structure.keys())
            for package in sorted_packages:
                outfile.write(f"## Пакет: {package}\n\n")
                for filename, filepath in sorted(package_structure[package]):
                    outfile.write(f"### {filename}\n")
                    try:
                        with open(filepath, "r", encoding="utf-8") as infile:
                            content = infile.read()
                            outfile.write("```java\n")
                            outfile.write(content)
                            outfile.write("\n```\n\n")
                    except Exception as e:
                        outfile.write(f"Ошибка чтения файла {filepath}: {e}\n\n")

        print(f"Информация о проекте сохранена в '{output_filename}'.")

    except Exception as e:
        print(f"Произошла ошибка при создании или записи в файл '{output_filename}': {e}")

if __name__ == "__main__":
    find_and_document_java_project()
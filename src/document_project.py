import os
import datetime

def find_and_document_java_project(start_dir=".", output_dir=".", output_prefix="project_info"):
    """
    Сканирует каталоги вверх от start_dir в поисках корневого каталога Java-проекта
    (наличие папки 'src' или файла pom.xml/build.gradle).
    Сохраняет структуру и содержимое всех найденных .java файлов в Markdown-файл
    с добавлением даты и времени в имя.

    Args:
        start_dir (str): Начальный каталог для поиска (по умолчанию: текущий каталог).
        output_dir (str): Каталог для сохранения Markdown-файла (по умолчанию: текущий каталог).
        output_prefix (str): Префикс для имени выходного файла (по умолчанию: 'project_info').
    """

    root_project_dir = None
    current_dir = os.path.abspath(start_dir)

    while current_dir:
        if os.path.exists(os.path.join(current_dir, "src")) or \
           os.path.exists(os.path.join(current_dir, "pom.xml")) or \
           os.path.exists(os.path.join(current_dir, "build.gradle")):
            root_project_dir = current_dir
            break
        parent_dir = os.path.dirname(current_dir)
        if parent_dir == current_dir:  # Достигнут корень файловой системы
            break
        current_dir = parent_dir

    if not root_project_dir:
        print("Корневой каталог Java-проекта не найден.")
        return

    print(f"Найден корневой каталог проекта: {root_project_dir}")

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

    timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
    output_filename = os.path.join(output_dir, f"{output_prefix}_{timestamp}.md")

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

if __name__ == "__main__":
    find_and_document_java_project()
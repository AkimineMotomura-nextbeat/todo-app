import { Component, OnInit } from '@angular/core';

import { Todo } from '../../models/todo';
import { Category } from 'src/app/models/category';
import { CategoryColor } from 'src/app/models/color';
import { TodoService } from '../../service/todo.service';
import { CategoryService } from 'src/app/service/category.service';

@Component({
  selector: 'app-todo-list',
  templateUrl: './todo-list.component.html',
  styleUrls: ['./todo-list.component.css']
})
export class TodoListComponent implements OnInit {

  todoList: Todo[] = [];
  categoryList: Category[] = [];
  colorList: CategoryColor[] = [];

  constructor(
    private todoService: TodoService,
    private categoryService: CategoryService
  ) { }

  ngOnInit(): void {
    this.getTodoList();
    this.getCategoryList();
    this.getColorList();
  }

  getTodoList(): void {
    this.todoService.getTodoList().subscribe(
      todoList => this.todoList = todoList
    )
  }

  getCategoryList(): void {
    this.categoryService.getCategoryList().subscribe(
      categoryList => this.categoryList = categoryList
    )
  }

  getColorList(): void {
    this.categoryService.getCategoryColorList().subscribe(
      colorList => this.colorList = colorList
    )
  }

  getCategoryColor(id: number): number {
    var categoryColor = 0;

    let category = this.categoryList.find(_ => _.id == id)
    if(category) categoryColor = category.color;

    return categoryColor;
  }

  getColorCode(id: number): string {
    var colorCode = "#eee";
    
    let color = this.colorList.find(_ => _.id == id);
    if(color) colorCode = color.colorCode;

    return colorCode;
  }

  delete(todo: Todo): void {
    this.todoList = this.todoList.filter(t => t !== todo);
    this.todoService.deleteTodo(todo.id).subscribe();
  }
}

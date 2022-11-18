import { Component, OnInit } from '@angular/core';

import { Todo } from '../../models/todo';
import { TodoService } from '../../service/todo.service';

@Component({
  selector: 'app-todo-list',
  templateUrl: './todo-list.component.html',
  styleUrls: ['./todo-list.component.css']
})
export class TodoListComponent implements OnInit {

  todoList: Todo[] = [];

  constructor(private todoService: TodoService) { }

  ngOnInit(): void {
    this.getTodoList();
  }

  getTodoList(): void {
    this.todoService.getTodoList().subscribe(
      todoList => this.todoList = todoList
    )
  }

  /*
  add(name: string): void {
    name = name.trim();
    if (!name) { return; }
    this.heroService.addHero({ name } as Hero)
      .subscribe(hero => {
        this.heroes.push(hero);
      });
  }
  */

  delete(todo: Todo): void {
    this.todoList = this.todoList.filter(t => t !== todo);
    this.todoService.deleteTodo(todo.id).subscribe();
  }
}

import { Component, OnInit, Input } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

import { Todo } from '../todo';
import { TodoService } from '../todo.service';

@Component({
  selector: 'app-category-detail',
  templateUrl: './todo-detail.component.html',
  styleUrls: ['./todo-detail.component.css']
})
export class TodoDetailComponent implements OnInit {

  @Input() todo?: Todo;

  constructor(
    private routes      : ActivatedRoute,
    private location    : Location,
    private todoService : TodoService
  ) { }

  ngOnInit(): void {
    //this.getTodo();

    //TODO debug
    this.todo = { id: 1, category: 1, title: 'Test1', content: 'hogehoge', state: 0}
  }

  goBack(): void {
    this.location.back();
  }

  getTodo(): void {
    const id = Number(this.routes.snapshot.paramMap.get('id'));
    this.todoService.getTodo(id).subscribe(todo => this.todo = todo)
  }

  save(): void {
    if (this.todo) {
      this.todoService.updateTodo(this.todo)
        .subscribe(() => this.goBack());
    }
  }
}
